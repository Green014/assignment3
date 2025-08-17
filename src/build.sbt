val javafxVersion = "21.0.4"
val scalafxVersion = "21.0.0-R32"

lazy val root = project
  .in(file("."))
  .settings(
    name := "farm-game",
    scalaVersion := "3.3.6",
    libraryDependencies ++= Seq(
      "org.scalafx" %% "scalafx" % scalafxVersion,
      "org.openjfx" % "javafx-base" % javafxVersion classifier "win",
      "org.openjfx" % "javafx-controls" % javafxVersion classifier "win",
      "org.openjfx" % "javafx-fxml" % javafxVersion classifier "win",
      "org.openjfx" % "javafx-graphics" % javafxVersion classifier "win",
      "org.openjfx" % "javafx-media" % javafxVersion classifier "win",
      "org.openjfx" % "javafx-swing" % javafxVersion classifier "win",
      "org.openjfx" % "javafx-web" % javafxVersion classifier "win"
    )
  )
