// Pipeline + groovy 
// import the Calcclulator class from com.i27academy.build package
import com.i27academy.builds.Calculator
import com.i27academy.builds.Docker

def call(Map pipelineParams){
    // An instance of the class called calculator is created
    Calculator calculator = new Calculator(this)
    Docker docker = new Docker(this)

// This Jenkinsfile is for Eureka Deployment

    pipeline {
        agent {
            label 'k8s-slave'
        }
        parameters {
            choice(name: 'scanOnly',
                choices: ['no', 'yes'],
                description: 'This will scan your application'
            )
            choice(name: 'buildOnly',
                choices: ['no', 'yes'],
                description: 'This will only build your application'
            )
            choice(name: 'dockerPush',
                choices: ['no', 'yes'],
                description: 'This will build dockerImage and push'
            )
            choice(name: 'deployToDev',
                choices: ['no', 'yes'],
                description: 'This will only Deploy the app to Dev env'
            )
            choice(name: 'deployToTest',
                choices: ['no', 'yes'],
                description: 'This will only Deploy the app to Test env'
            )
            choice(name: 'deployToStage',
                choices: ['no', 'yes'],
                description: 'This will only Deploy the app to stage env'
            )
            choice(name: 'deployToProd',
                choices: ['no', 'yes'],
                description: 'This will only Deploy the app to Prod env'
            )
        }
        tools {
            maven 'Maven-3.9.11'
            jdk 'JDK-17'
        }
        environment {
            APPLICATION_NAME = "${pipelineParams.appName}"
            SONAR_TOKEN = credentials('sonar_creds')
            SONAR_URL = "http://34.46.97.238:9000"
            // https://www.jenkins.io/doc/pipeline/steps/pipeline-utility-steps/#readmavenpom-read-a-maven-project-file
            // If any errors with readMavenPom, make sure pipeline-utility-steps plugin is installed in your jenkins, if not do install it
            // Script Approval issues : http://34.148.12.185:8080/scriptApproval/
            POM_VERSION = readMavenPom().getVersion()
            POM_PACKAGING = readMavenPom().getPackaging()
            DOCKER_HUB = "docker.io/kommuamar1133"
            DOCKER_CREDS = credentials('dockerhub_creds')
        }
        //Stages
        stages {
            stage ('Build') {
                when {
                    anyOf {
                        expression {
                            params.dockerPush == 'yes'
                            params.buildOnly == 'yes'
                        }
                    }
                }
                steps {
                    script {
                        docker.buildApp("${env.APPLICATION_NAME}")   //appName
                    }
                }
            }
            stage ('Sonar') {
                when {
                    expression {
                        params.scanOnly == 'yes'
                        // params.buildOnly == 'yes'
                        // params.dockerPush == 'yes'
                    }
                }
                steps {
                    script {
                        sonar().call()
                    }
                }
            }
            stage ('Docker Build & Push') {
                when {
                    anyOf {
                        expression {
                            params.dockerPush == 'yes'
                        }
                    }
                }
                steps {
                    script {
                        dockerBuildAndPush().call()
                    }
                }

            }
            stage ('Deploy to Dev-Server') {
                when {
                    anyOf {
                        expression {
                            params.deployToDev == 'yes'
                        }
                    }
                }
                steps {
                    script {
                        // envDeploy, hostPort, contPort
                        imageValidation().call()
                        dockerDeploy('dev', '5761', '8761').call()
                    }
                }
            }
            stage ('Deploy to Test-Server') {
                when {
                    anyOf {
                        expression {
                            params.deployToTest == 'yes'
                        }
                    }
                }
                steps {
                    script {
                        // envDeploy, hostPort, contPort
                        imageValidation().call()
                        dockerDeploy('tst', '6761', '8761').call()
                    }       
                }
            }
            stage ('Deploy to Stage-Server') {
                when {
                    allOf {
                        anyOf {
                            expression {
                                params.deployToStage == 'yes'
                                //other condition
                            }
                        }
                        anyOf {
                            branch 'release/*'
                        }
                    }
                }
                steps {
                    script {
                        // envDeploy, hostPort, contPort
                        imageValidation().call()
                        dockerDeploy('stg', '7761', '8761').call()
                    }         
                }
            }
            stage ('Deploy to Prod-Server') {
                // Make sure only tags are deployed?
                when {
                    allOf {
                        anyOf {
                            expression {
                                params.deployToProd == 'yes'
                            }
                        }
                        anyOf {
                            tag pattern: "v\\d{1,2}\\.\\d{1,2}\\.\\d{1,2}", comparator: "REGEXP"  //v1.2.3
                        }
                    }
                }
                steps { 
                    timeout(time: 300 , unit: 'SECONDS' ) {  //SECONDS, MINUTES, HOURS
                        input message: "Deploying to ${APPLICATION_NAME} to production ??", ok: 'yes', submitter: 'nanisre'
                    }  
                    script {
                        // envDeploy, hostPort, contPort
                        imageValidation().call()
                        dockerDeploy('prd', '8761', '8761').call()
                    }       
                }
            }       
        }
    }
}








// container port = 8761

// dev hp = 5761
// tst hp = 6761
// stg hp = 7761
// prod hp = 8761







//We need to connect to the dockerserver through the jenkinsslave using below command:
//withCredentials([usernameColonPassword(credentialsId: 'mylogin', variable: 'USERPASS')]) {
  // sshpass -p password ssh -o StrictHostKeyChecking=no username@dockerserver_ip



// usernameVariable : String
// Name of an environment variable to be set to the username during the build.
// passwordVariable : String
// Name of an environment variable to be set to the password during the build.
// credentialsId : String
// Credentials of an appropriate type to be set to the variable.
