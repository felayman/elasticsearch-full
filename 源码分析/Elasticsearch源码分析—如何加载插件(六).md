
>  Elasticsearch版本为 5.5.0,下面是主要的核心流程,忽略异常补偿部分


     Elasticsearch能够对外提供很多非内置的服务,比如我们可以使用x-pack,head,sql,marvel等帮助我们对Elasticsearch集群进行监控和发送命令给集群,所有这些服务,
     Elasticsearch都是以可插拔的插件方式对开发者开放,开发者可以根据不同的业务需求,定制开发各种插件,这些插件在 Elasticsearch启动的过程中加载到自身环境中,
     然后以插件的方式对外提供服务,那么这些插件是如何被 Elasticsearch加载呢?

  ### 节点启动过程中加载 Elasticsearch的安装目录下的plugins和module

  ~~~java
  1. this.pluginsService = new PluginsService(tmpSettings, environment.modulesFile(), environment.pluginsFile(), classpathPlugins);
  2.  public PluginsService(Settings settings, Path modulesDirectory, Path pluginsDirectory, Collection<Class<? extends Plugin>> classpathPlugins) {
             super(settings);

             List<Tuple<PluginInfo, Plugin>> pluginsLoaded = new ArrayList<>();
             List<PluginInfo> pluginsList = new ArrayList<>();
             // first we load plugins that are on the classpath. this is for tests and transport clients
             for (Class<? extends Plugin> pluginClass : classpathPlugins) {
                 Plugin plugin = loadPlugin(pluginClass, settings);
                 PluginInfo pluginInfo = new PluginInfo(pluginClass.getName(), "classpath plugin", "NA", pluginClass.getName(), false);
                 if (logger.isTraceEnabled()) {
                     logger.trace("plugin loaded from classpath [{}]", pluginInfo);
                 }
                 pluginsLoaded.add(new Tuple<>(pluginInfo, plugin));
                 pluginsList.add(pluginInfo);
             }

             Set<Bundle> seenBundles = new LinkedHashSet<>();
             List<PluginInfo> modulesList = new ArrayList<>();
             // load modules
             if (modulesDirectory != null) {
                 try {
                     Set<Bundle> modules = getModuleBundles(modulesDirectory);
                     for (Bundle bundle : modules) {
                         modulesList.add(bundle.plugin);
                     }
                     seenBundles.addAll(modules);
                 } catch (IOException ex) {
                     throw new IllegalStateException("Unable to initialize modules", ex);
                 }
             }

             // now, find all the ones that are in plugins/
             if (pluginsDirectory != null) {
                 try {
                     Set<Bundle> plugins = getPluginBundles(pluginsDirectory);
                     for (Bundle bundle : plugins) {
                         pluginsList.add(bundle.plugin);
                     }
                     seenBundles.addAll(plugins);
                 } catch (IOException ex) {
                     throw new IllegalStateException("Unable to initialize plugins", ex);
                 }
             }

             List<Tuple<PluginInfo, Plugin>> loaded = loadBundles(seenBundles);
             pluginsLoaded.addAll(loaded);

             this.info = new PluginsAndModules(pluginsList, modulesList);
             this.plugins = Collections.unmodifiableList(pluginsLoaded);

             // We need to build a List of plugins for checking mandatory plugins
             Set<String> pluginsNames = new HashSet<>();
             for (Tuple<PluginInfo, Plugin> tuple : this.plugins) {
                 pluginsNames.add(tuple.v1().getName());
             }

             // Checking expected plugins
             List<String> mandatoryPlugins = MANDATORY_SETTING.get(settings);
             if (mandatoryPlugins.isEmpty() == false) {
                 Set<String> missingPlugins = new HashSet<>();
                 for (String mandatoryPlugin : mandatoryPlugins) {
                     if (!pluginsNames.contains(mandatoryPlugin) && !missingPlugins.contains(mandatoryPlugin)) {
                         missingPlugins.add(mandatoryPlugin);
                     }
                 }
                 if (!missingPlugins.isEmpty()) {
                     throw new ElasticsearchException("Missing mandatory plugins [" + Strings.collectionToDelimitedString(missingPlugins, ", ") + "]");
                 }
             }

             // we don't log jars in lib/ we really shouldn't log modules,
             // but for now: just be transparent so we can debug any potential issues
             logPluginInfo(info.getModuleInfos(), "module", logger);
             logPluginInfo(info.getPluginInfos(), "plugin", logger);
         }
  ~~~

上述代码的大概流程如下:

1. 加载classpath下的插件
2. 查找Elasticsearch安装目录下的modules目录下的插件(module和plugin在本质上是同一个东西,module是内置服务,plugin是对外提供服务)
3. 查找Elasticsearch安装目录下的plugins目录下的插件
4. 合并3、4步骤中查找到的插件,一起加载到Elasticsearch环境中—这是重点


我们来看看Elasticsearch是如何加载这些modules和plugins的(modules和plugins绑定一起后的名称为Bundle),调用方法为List<Tuple<PluginInfo, Plugin>> loaded = loadBundles(seenBundles),源码如下：

~~~java
private List<Tuple<PluginInfo,Plugin>> loadBundles(Set<Bundle> bundles) {
        List<Tuple<PluginInfo, Plugin>> plugins = new ArrayList<>();

        for (Bundle bundle : bundles) {
            // jar-hell check the bundle against the parent classloader
            // pluginmanager does it, but we do it again, in case lusers mess with jar files manually
            try {
                Set<URL> classpath = JarHell.parseClassPath();
                // check we don't have conflicting codebases
                Set<URL> intersection = new HashSet<>(classpath);
                intersection.retainAll(bundle.urls);
                if (intersection.isEmpty() == false) {
                    throw new IllegalStateException("jar hell! duplicate codebases between" +
                                                    " plugin and core: " + intersection);
                }
                // check we don't have conflicting classes
                Set<URL> union = new HashSet<>(classpath);
                union.addAll(bundle.urls);
//                JarHell.checkJarHell(union);
            } catch (Exception e) {
                throw new IllegalStateException("failed to load plugin " + bundle.plugin +
                                                " due to jar hell", e);
            }

            // create a child to load the plugin in this bundle
            ClassLoader loader = URLClassLoader.newInstance(bundle.urls.toArray(new URL[0]),
                                                            getClass().getClassLoader());
            // reload lucene SPI with any new services from the plugin
            reloadLuceneSPI(loader);
            final Class<? extends Plugin> pluginClass =
                loadPluginClass(bundle.plugin.getClassname(), loader);
            final Plugin plugin = loadPlugin(pluginClass, settings);
            plugins.add(new Tuple<>(bundle.plugin, plugin));
        }

        return Collections.unmodifiableList(plugins);
    }
~~~

整个加载流程大概如下:

1. 创建一个类加载器,用于加载插件中所依赖的jar包
2. 重新加载Lucene的SPI服务
3. 用创建好的类加载器加载插件类
4. 用加载好的插件类创建插件服务(就是创建插件实例,用于对外提供服务)
