Embulk::JavaPlugin.register_input(
  "randomj", "org.embulk.input.randomj.RandomjInputPlugin",
  File.expand_path('../../../../classpath', __FILE__))
