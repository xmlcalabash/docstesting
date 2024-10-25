import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.io.PrintStream
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import com.nwalsh.gradle.saxon.SaxonXsltTask
import com.nwalsh.gradle.relaxng.validate.RelaxNGValidateTask
import com.nwalsh.gradle.relaxng.translate.RelaxNGTranslateTask

buildscript {
  repositories {
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://maven.saxonica.com/maven") }
  }

  configurations.all {
    resolutionStrategy.eachDependency {
      if (requested.group == "xml-apis" && requested.name == "xml-apis") {
        useVersion("1.4.01")
      }
      if (requested.group == "net.sf.saxon" && requested.name == "Saxon-HE") {
        useVersion(project.properties["saxonVersion"].toString())
      }
      if (requested.group == "org.xmlresolver" && requested.name == "xmlresolver") {
        useVersion(project.properties["xmlResolverVersion"].toString())
      }
    }
  }

  dependencies {
    classpath("net.sf.saxon:Saxon-HE:${project.properties["saxonVersion"]}")
    classpath("org.docbook:schemas-docbook:5.2")
    classpath("org.docbook:docbook-xslTNG:2.4.0")
  }
}

plugins {
  id("com.nwalsh.gradle.saxon.saxon-gradle") version "0.10.4"
  id("com.nwalsh.gradle.relaxng.validate") version "0.10.3"
  id("com.nwalsh.gradle.relaxng.translate") version "0.10.5"
  id("buildlogic.kotlin-common-conventions")
}

val saxonVersion = project.properties["saxonVersion"].toString()
val buildTime = ZonedDateTime.now(ZoneId.of("UTC")).truncatedTo(ChronoUnit.SECONDS);
val iso8601dt = DateTimeFormatter.ISO_INSTANT.format(buildTime)

val guideVersion = "1.1.0"
val refVersion = "1.0.0"

repositories {
  mavenLocal()
  mavenCentral()
}

configurations.all {
  resolutionStrategy.eachDependency {
    if (requested.group == "xml-apis" && requested.name == "xml-apis") {
      useVersion("1.4.01")
    }
    if (requested.group == "net.sf.saxon" && requested.name == "Saxon-HE") {
      useVersion(saxonVersion)
    }
    if (requested.group == "org.xmlresolver" && requested.name == "xmlresolver") {
      useVersion(project.properties["xmlResolverVersion"].toString())
    }
  }
}

val documentation by configurations.creating
val transform by configurations.creating {
  extendsFrom(configurations["documentation"])
}

dependencies {
  documentation ("net.sf.saxon:Saxon-HE:${saxonVersion}")
  documentation ("org.docbook:schemas-docbook:5.2")
  documentation ("org.docbook:docbook-xslTNG:2.4.0")
}

// This build script doesn't do very much yet.

defaultTasks("publish")

tasks.register("publish") {
  dependsOn("copyStaticFiles")
  dependsOn("userGuide")
  dependsOn("reference")
}

tasks.register("copyStaticFiles") {
  doFirst {
    copy {
      from(layout.projectDirectory.dir("src/main/html"))
      into(layout.buildDirectory.dir("website"))
    }
  }
  doFirst {
    copy {
      from(layout.projectDirectory.dir("src/main/css"))
      into(layout.buildDirectory.dir("website/css"))
    }
  }
  doFirst {
    copy {
      from(layout.projectDirectory.dir("src/main/img"))
      into(layout.buildDirectory.dir("website/img"))
    }
  }
}

tasks.register("userGuide") {
  doFirst {
    mkdir("documentation/build/userguide")
  }

  doLast {
    val stream = PrintStream(File("documentation/build/userguide/index.html"))
    stream.println("<html><head><title>UserGuide</title></head><body><p>")
    stream.println("User guide version ${guideVersion}.<br/>")
    stream.println(iso8601dt)
    stream.println("</p></body></html>")
    stream.close()
  }

  doLast {
    val stream = PrintStream(File("documentation/build/userguide/details.json"))
    stream.println("{\"version\": \"${guideVersion}\", \"pubdate\": \"${iso8601dt}\"}")
    stream.close()
  }
}

tasks.register("reference") {
  doFirst {
    mkdir("documentation/build/reference")
  }

  doLast {
    val stream = PrintStream(File("documentation/build/reference/index.html"))
    stream.println("<html><head><title>Reference</title></head><body><p>")
    stream.println("Reference version ${refVersion}.<br/>")
    stream.println(iso8601dt)
    stream.println("</p></body></html>")
    stream.close()
  }

  doLast {
    val stream = PrintStream(File("documentation/build/reference/details.json"))
    stream.println("{\"version\": \"${refVersion}\", \"pubdate\": \"${iso8601dt}\"}")
    stream.close()
  }
}

// ============================================================

tasks.register("helloWorld") {
  doLast {
    println("Hello, world.")
  }
}
