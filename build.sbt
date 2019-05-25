lazy val akkaHttpVersion = "10.1.8"
lazy val akkaVersion = "2.6.0-M2"
lazy val scalaTestVersion = "3.0.7"
lazy val catsVersion = "1.6.0"
lazy val slickVersion = "3.3.0"
lazy val h2Version = "1.4.192"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "io.azhur.scalacamp",
      scalaVersion    := "2.12.8"
    )),
    name := "ScalaCampAkkaHttp",
    libraryDependencies ++= Seq(
      "com.typesafe.akka"  %% "akka-http"            % akkaHttpVersion,
      "com.typesafe.akka"  %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka"  %% "akka-http-xml"        % akkaHttpVersion,
      "com.typesafe.akka"  %% "akka-stream"          % akkaVersion,

      "org.typelevel"      %% "cats-core"            % catsVersion,

      "com.typesafe.slick" %% "slick"                % slickVersion,
      "com.typesafe.slick" %% "slick-hikaricp"       % slickVersion,

      "com.typesafe.akka"  %% "akka-http-testkit"    % akkaHttpVersion  % Test,
      "com.typesafe.akka"  %% "akka-testkit"         % akkaVersion      % Test,
      "com.typesafe.akka"  %% "akka-stream-testkit"  % akkaVersion      % Test,
      "org.scalatest"      %% "scalatest"            % scalaTestVersion % Test,
      "com.h2database"     %  "h2"                   % h2Version        % Test
    )
  )
