pipeline {
	agent none

	triggers {
		pollSCM 'H/10 * * * *'
	}

	options {
		disableConcurrentBuilds()
	}

	stages {
		stage('Publish OpenJDK 8 + Graphviz + jq docker image') {
			when {
				changeset "ci/Dockerfile"
			}
			agent any

			steps {
				script {
					def image = docker.build("springci/spring-hateoas-openjdk8-with-graphviz-and-jq", "ci/")
					docker.withRegistry('', 'hub.docker.com-springbuildmaster') {
						image.push()
					}
				}
			}
		}

		stage("test: baseline (jdk8)") {
			agent {
				docker {
					image 'adoptopenjdk/openjdk8:latest'
					args '-v $HOME/.m2:/root/.m2'
				}
			}
			steps {
				sh "PROFILE=none ci/test.sh"
			}
		}

		stage("Test other configurations") {
			parallel {
				stage("test: baseline (jdk11)") {
					agent {
						docker {
							image 'adoptopenjdk/openjdk11:latest'
							args '-v $HOME/.m2:/root/.m2'
						}
					}
					steps {
						sh "PROFILE=none ci/test.sh"
					}
				}
				stage("test: baseline (jdk12)") {
					agent {
						docker {
							image 'adoptopenjdk/openjdk12:latest'
							args '-v $HOME/.m2:/root/.m2'
						}
					}
					steps {
						sh "PROFILE=none ci/test.sh"
					}
				}
				stage("test: spring-next (jdk8)") {
					agent {
						docker {
							image 'adoptopenjdk/openjdk8:latest'
							args '-v $HOME/.m2:/root/.m2'
						}
					}
					steps {
						sh "PROFILE=spring-next ci/test.sh"
					}
				}
				stage("test: spring-next (jdk11)") {
					agent {
						docker {
							image 'adoptopenjdk/openjdk11:latest'
							args '-v $HOME/.m2:/root/.m2'
						}
					}
					steps {
						sh "PROFILE=spring-next ci/test.sh"
					}
				}
				stage("test: spring-next (jdk12)") {
					agent {
						docker {
							image 'adoptopenjdk/openjdk12:latest'
							args '-v $HOME/.m2:/root/.m2'
						}
					}
					steps {
						sh "PROFILE=spring-next ci/test.sh"
					}
				}
			}
		}

		stage('Deploy to Artifactory') {
			agent {
				docker {
					image 'springci/spring-hateoas-openjdk8-with-graphviz-and-jq:latest'
					args '-v $HOME/.m2:/root/.m2'
				}
			}

			environment {
				ARTIFACTORY = credentials('02bd1690-b54f-4c9f-819d-a77cb7a9822c')
			}

			steps {
				script {
					// Warm up this plugin quietly before using it.
					sh "./mvnw -q org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version"

					PROJECT_VERSION = sh(
							script: "./mvnw org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version -o | grep -v INFO",
							returnStdout: true
					).trim()

					RELEASE_TYPE = 'milestone' // .RC? or .M?

					if (PROJECT_VERSION.endsWith('BUILD-SNAPSHOT')) {
						RELEASE_TYPE = 'snapshot'
					} else if (PROJECT_VERSION.endsWith('RELEASE')) {
						RELEASE_TYPE = 'release'
					}

					OUTPUT = sh(
							script: "PROFILE=ci,${RELEASE_TYPE} ci/build.sh",
							returnStdout: true
					).trim()

					echo "$OUTPUT"

					build_info_path = OUTPUT.split('\n')
							.find { it.contains('Artifactory Build Info Recorder') }
							.split('Saving Build Info to ')[1]
							.trim()[1..-2]

					dir(build_info_path + '/..') {
						stash name: 'build_info', includes: "*.json"
					}
				}
			}
		}
		stage('Promote to Bintray') {
			when {
				branch 'release'
			}
			agent {
				docker {
					image 'springci/spring-hateoas-openjdk8-with-graphviz-and-jq:latest'
					args '-v $HOME/.m2:/root/.m2'
				}
			}

			environment {
				ARTIFACTORY = credentials('02bd1690-b54f-4c9f-819d-a77cb7a9822c')
			}

			steps {
				script {
					// Warm up this plugin quietly before using it.
					sh "./mvnw -q org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version"

					PROJECT_VERSION = sh(
							script: "./mvnw org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version -o | grep -v INFO",
							returnStdout: true
					).trim()

					if (PROJECT_VERSION.endsWith('RELEASE')) {
						unstash name: 'build_info'
						sh "ci/promote-to-bintray.sh"
					} else {
						echo "${PROJECT_VERSION} is not a candidate for promotion to Bintray."
					}
				}
			}
		}
		stage('Sync to Maven Central') {
			when {
				branch 'release'
			}
			agent {
				docker {
					image 'springci/spring-hateoas-openjdk8-with-graphviz-and-jq:latest'
					args '-v $HOME/.m2:/root/.m2'
				}
			}

			environment {
				BINTRAY = credentials('Bintray-spring-operator')
				SONATYPE = credentials('oss-token')
			}

			steps {
				script {
					// Warm up this plugin quietly before using it.
					sh "./mvnw -q org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version"

					PROJECT_VERSION = sh(
							script: "./mvnw org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version -o | grep -v INFO",
							returnStdout: true
					).trim()

					if (PROJECT_VERSION.endsWith('RELEASE')) {
						unstash name: 'build_info'
						sh "ci/sync-to-maven-central.sh"
					} else {
						echo "${PROJECT_VERSION} is not a candidate for syncing to Maven Central."
					}
				}
			}
		}
	}

	post {
		changed {
			script {
				slackSend(
						color: (currentBuild.currentResult == 'SUCCESS') ? 'good' : 'danger',
						channel: '#spring-hateoas',
						message: "${currentBuild.fullDisplayName} - `${currentBuild.currentResult}`\n${env.BUILD_URL}")
				emailext(
						subject: "[${currentBuild.fullDisplayName}] ${currentBuild.currentResult}",
						mimeType: 'text/html',
						recipientProviders: [[$class: 'CulpritsRecipientProvider'], [$class: 'RequesterRecipientProvider']],
						body: "<a href=\"${env.BUILD_URL}\">${currentBuild.fullDisplayName} is reported as ${currentBuild.currentResult}</a>")
			}
		}
	}
}
