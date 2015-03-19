project(modelVersion: '4.0.0', groupId: 'org.springframework.hateoas', artifactId: 'spring-hateoas') {

  version '0.18.0.BUILD-SNAPSHOT'

  name 'Spring HATEOAS'
  description 'Library to support implementing representations for hyper-text driven REST web services.'
  url 'http://github.com/SpringSource/spring-hateoas'
  inceptionYear '2012-2015'

  organization(name: 'Pivotal, Inc.', url: 'http://www.spring.io')

  licenses {
    license {
      name 'Apache License, Version 2.0'
      url 'http://www.apache.org/licenses/LICENSE-2.0'
      comments '''Copyright 2011 the original author or authors.

			Licensed under the Apache License, Version 2.0 (the "License");
			you may not use this file except in compliance with the License.
			You may obtain a copy of the License at

			     http://www.apache.org/licenses/LICENSE-2.0

			Unless required by applicable law or agreed to in writing, software
			distributed under the License is distributed on an "AS IS" BASIS,
			WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
			implied.
			See the License for the specific language governing permissions and
			limitations under the License.'''
    }
  }

  developers {
    developer(id: 'ogierke', name: 'Oliver Gierke', email: 'ogierke(at)pivotal.io', organization: 'Pivotal, Inc.', timezone: '+1') {roles {
        role 'Project lead'
      }
    }
  }

  scm {
    connection 'scm:git:git://github.com/spring-projects/spring-hateoas.git'
    developerConnection 'scm:git:ssh://git@github.com:spring-projects/spring-hateoas.git'
    url 'https://github.com/spring-projects/spring-hateoas'
  }

  properties {
    'objenesis.version' '2.1'
    'slf4j.version' '1.7.10'
    'evo.version' '1.2.1'
    'jaxrs.version' '1.0'
    'source.level' '1.6'
    'bundlor.failOnWarnings' 'true'
    'project.build.sourceEncoding' 'UTF-8'
    'logback.version' '1.1.2'
    'jsonpath.version' '0.9.1'
    'jackson.version' '2.4.3'
    'minidevjson.version' '2.1.0'
    'spring.version' '4.0.9.RELEASE'
  }

  dependencies {

    dependency 'org.springframework:spring-aop:${spring.version}'
    dependency 'org.springframework:spring-beans:${spring.version}'
    dependency 'org.springframework:spring-context:${spring.version}'
    dependency 'org.springframework:spring-web:${spring.version}'
    dependency 'org.springframework:spring-webmvc:${spring.version}'
    dependency('org.springframework:spring-core:${spring.version}') {
      exclusions 'commons-logging:commons-logging'
    }

    dependency 'org.springframework.plugin:spring-plugin-core:1.1.0.RELEASE'
    dependency 'org.objenesis:objenesis:${objenesis.version}'

    dependency 'com.fasterxml.jackson.core:jackson-annotations:${jackson.version}'
    dependency 'com.fasterxml.jackson.core:jackson-databind:${jackson.version}'

    dependency('javax.ws.rs:jsr311-api:${jaxrs.version}') { optional 'true' }
    dependency('com.jayway.jsonpath:json-path:${jsonpath.version}') { optional 'true' }
    dependency('org.atteo:evo-inflector:${evo.version}') { optional 'true' }
    dependency('org.projectlombok:lombok:1.14.4') { scope 'provided' }

    dependency 'org.slf4j:slf4j-api:${slf4j.version}'
    dependency('org.slf4j:jcl-over-slf4j:${slf4j.version}') { scope 'test' }
    dependency('ch.qos.logback:logback-classic:${logback.version}') { scope 'test' }

    dependency('org.hamcrest:hamcrest-library:1.3') { scope 'test' }
    dependency('junit:junit:4.11') { scope 'test' }
    dependency('org.springframework:spring-test:${spring.version}') { scope 'test' }

    dependency('org.mockito:mockito-all:1.9.5') { scope 'test' }
    dependency('joda-time:joda-time:2.3') { scope 'test' }
    dependency('xmlunit:xmlunit:1.5') { scope 'test' }
    dependency('net.jadler:jadler-all:1.1.1') { scope 'test' }
    dependency('javax.servlet:servlet-api:2.5')  { scope 'provided' }
  }

  repositories {
    repository(id: 'spring-libs-release', url: 'http://repo.spring.io/libs-release')
  }

  pluginRepositories {
    pluginRepository(id: 'spring-plugins-release', url: 'http://repo.spring.io/plugins-release')
  }

  build {

    extensions 'org.apache.maven.wagon:wagon-ssh:2.5'

    plugins {

      plugin('org.apache.maven.plugins:maven-compiler-plugin:3.2') {
        configuration {
          source '${source.level}'
          target '${source.level}'
        }
      }

      plugin('com.springsource.bundlor:com.springsource.bundlor.maven:1.0.0.RELEASE') {
        executions {
          execution(id: 'bundlor', goal: 'bundlor')
        }
        configuration {
          failOnWarnings '${bundlor.failOnWarnings}'
        }
      }

      plugin('org.apache.maven.plugins:maven-jar-plugin:2.5') {
        configuration {
          useDefaultManifestFile 'true'
        }
      }

      plugin('org.apache.maven.plugins:maven-source-plugin:2.4') {
        executions {
          execution(id: 'attach-sources', goal: 'jar')
        }
      }

      plugin('org.apache.maven.plugins:maven-javadoc-plugin:2.10.1') {
        configuration {
          breakiterator 'true'
          header '${project.name}'
          source '${source.level}'
          quiet 'true'
          additionalparam '-Xdoclint:none'
          stylesheetfile '${shared.resources}/javadoc/spring-javadoc.css'
          links {
            link 'http://static.springframework.org/spring/docs/3.2.x/javadoc-api'
            link 'http://docs.oracle.com/javase/6/docs/api'
          }
        }
      }

      plugin('org.apache.maven.plugins:maven-deploy-plugin:2.8.1') {
        configuration {
          skip 'true'
        }
      }
    }
  }

  profiles {

    profile(id: 'spring40-next') {
      properties {
        'spring.version' '4.0.10.BUILD-SNAPSHOT'
      }
      repositories {
        repository(id: 'spring-libs-snapshot', url: 'http://repo.spring.io/libs-snapshot')
      }
    }

    profile(id: 'spring41') {
      properties {
        'spring.version' '4.1.5.RELEASE'
      }
    }

    profile(id: 'spring41-next') {
      properties {
        'spring.version' '4.1.6.BUILD-SNAPSHOT'
      }
      repositories {
        repository(id: 'spring-libs-snapshot', url: 'http://repo.spring.io/libs-snapshot')
      }
    }

    profile(id: 'spring42-next') {
      properties {
        'spring.version' '4.2.0.BUILD-SNAPSHOT'
      }
      repositories {
        repository(id: 'spring-libs-snapshot', url: 'http://repo.spring.io/libs-snapshot')
      }
    }

    profile(id: 'ci') {
      build {
        plugins {
          plugin('org.apache.maven.plugins:maven-javadoc-plugin:2.10.1') {
            executions {
              execution(phase: 'package', goal: 'jar')
            }
          }
        }
      }
    }


    profile(id: 'distribute') {

      build {
        plugins {

          plugin('org.apache.maven.plugins:maven-dependency-plugin') {
            executions {
              execution(id: 'unpack-shared-resources', phase: 'generate-resources', goal: 'unpack-dependencies')
            }
            configuration {
              includeGroupIds 'org.springframework.data'
              includeArtifacIds 'spring-data-build-resources'
              includeTypes 'zip'
              excludeTransitive 'true'
              outputDirectory '${shared.resources}'
            }
          }

          plugin('org.apache.maven.plugins:maven-javadoc-plugin') {
            executions {
              execution(id: 'aggregate-javadoc', phase: 'package', goal: 'aggregate')
            }
          }

          plugin('org.asciidoctor:asciidoctor-maven-plugin:1.5.2') {
            executions {
              execution(id: 'html', phase: 'generate-resources', goal: 'process-asciidoc') {
                configuration {
                  backend 'html5'
                  outputDirectory '${project.build.directory}/site/reference/html'
                  sectids 'false'
                  sourceHighlighter 'prettify'
                  attributes {
                    linkcss 'true'
                    icons 'font'
                    sectanchors 'true'
                    stylesheet 'spring.css'
                  }
                }
              }
              execution(id: 'pdf', phase: 'generate-resources', goal: 'process-asciidoc') {
                configuration {
                  backend 'pdf'
                  sourceHighlighter 'coderay'
                }
              }
            }

            dependencies {
              dependency 'org.asciidoctor:asciidoctorj-pdf:1.5.0-alpha.6'
              dependency 'org.asciidoctor:asciidoctorj-epub3:1.5.0-alpha.4'
            }

            configuration {
              sourceDirectory '${project.root}/src/main/asciidoc'
              sourceDocumentName 'index.adoc'
              doctype 'book'
              attributes {
                version '${project.version}'
                projectName '${project.name}'
                projectVersion '${project.version}'
                'allow-uri-read' 'true'
                toclevels '3'
                numbered 'true'
              }
            }
          }

          plugin('org.apache.maven.plugins:maven-antrun-plugin:1.7') {
            executions {

              execution(id: 'copy-documentation-resources', phase: 'generate-resources', goal: 'run') {
                configuration {
                  target {
                    copy(todir:'${project.root}/target/site/reference/html') {
                      fileset(dir:'${shared.resources}/asciidoc', erroronmissingdir:'false') {
                        include(name:'**/*.css')
                      }
                      flattenmapper {}
                    }
                    copy(todir:'${project.root}/target/site/reference/html/images') {
                      fileset(dir:'${basedir}/src/main/asciidoc', erroronmissingdir:'false') {
                        include(name:'**/*.png')
                        include(name:'**/*.gif')
                        include(name:'**/*.jpg')
                      }
                      flattenmapper {}
                    }
                  }
                }
              }

              execution(id: 'rename-reference-docs', phase: 'process-resources', goal: 'run') {
                configuration {
                  target {
                    copy(failonerror:'false', file:'${project.build.directory}/generated-docs/index.pdf', tofile:'${project.root}/target/site/reference/pdf/${project.artifactId}-reference.pdf')
                    copy(failonerror:'false', file:'${project.build.directory}/generated-docs/index.epub', tofile:'${project.root}/target/site/reference/epub/${project.artifactId}-reference.epub')
                  }
                }
              }
            }
          }

          plugin('org.apache.maven.plugins:maven-assembly-plugin:2.4') {
            executions {
              execution(id: 'static', phase: 'package', goal: 'single') {
                configuration {
                  descriptors {
                    descriptor '${shared.resources}/assemblies/static-resources.xml'
                  }
                  finalName 'static-resources'
                  appendAssemblyId 'false'
                }
              }
            }
          }

          plugin('org.codehaus.mojo:wagon-maven-plugin:1.0-beta-5') {
            executions {
              execution(id: 'upload-static-resources', phase: 'deploy', goal: 'upload') {
                configuration {
                  fromDir '${project.build.directory}/static-resources'
                  includes '**'
                  serverId 'static-dot-s2'
                  url 'scp://static.springsource.org'
                  toDir '/var/www/domains/springsource.org/www/htdocs/autorepo/docs/${project.artifactId}/${project.version}'
                  optimize 'true'
                }
              }
            }
            configuration {
              fromDir '${project.build.directory}'
            }
          }
        }

      }

      properties {
        'skipTests' 'true'
        'maven.install.skip' 'true'
        'shared.resources' '${project.build.directory}/shared-resources'
        'project.root' '${basedir}'
      }

      dependencies {
        dependency('org.springframework.data.build:spring-data-build-resources:1.5.2.RELEASE') {
          type 'zip'
          scope 'provided'
        }
      }
    }
  }
}
