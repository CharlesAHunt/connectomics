name := "connectomics"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.mongodb" %% "casbah" % "2.7.0-RC0",
  "org.jasypt" % "jasypt" % "1.9.1",
  "com.novus" %% "salat" % "1.9.5"
)     

play.Project.playScalaSettings
