package com.i27academy.builds;

// all the methods 
class Docker {
    def jenkins
    Docker(jenkins) {
        this.jenkins = jenkins
    }

    // Application Build
    def buildApp(appName){
        jenkins.sh """
            echo "Building the $appName Application"
            mvn clean package -DskipTests=true
        """  
    }


}





//
// def buildApp(){
//     return {
//         echo "Building the ${env.APPLICATION_NAME} Application"
//         sh 'mvn clean package -DskipTests=true'
//         // After building if you face any issue with mvn installation? --> There is issue with the permissions (chown -R <kommuamar1133>:<kommuamar1133> <apache maven) of maven in /opt/apcahe_maven
//     }
// }

//
// def sonar(){
//     return {
//         echo "Starting Sonar Scan"
//         withSonarQubeEnv('SonarQube'){  // The name you saved in system under manage jenkins
//             sh """
//             mvn sonar:sonar \
//                 -Dsonar.projectkey=i27-eureka \
//                 -Dsonar.host.url=${env.SONAR_URL} \
//                 -Dsonar.login=${SONAR_TOKEN}
//             """
//         }  
//         timeout (time: 2, unit: 'MINUTES'){
//             waitForQualityGate abortPipeline: true
//         }
//     }
// }

// Method for docker build & push
// def dockerBuildAndPush(){
//     return {
//         // Existing artifact format: i27-eureka-0.0.1-SNAPSHOT.jar
//         // My Destination artifact format: i27-eureka-buildnumber-branchname.jar
//         echo "My JAR file SOURCE: i27-${env.APPLICATION_NAME}-${env.POM_VERSION}.${env.POM_PACKAGING}"
//         echo "My JAR Destination: i27-${env.APPLICATION_NAME}-${BUILD_NUMBER}-${BRANCH_NAME}.${env.POM_PACKAGING}"
//         sh """
//             echo "***********************Building Docker Image*************************"
//             pwd
//             ls -la
//             cp ${WORKSPACE}/target/i27-${env.APPLICATION_NAME}-${env.POM_VERSION}.${env.POM_PACKAGING}  ./.cicd
//             ls -la ./.cicd
//             docker build --no-cache --build-arg JAR_SOURCE=i27-${env.APPLICATION_NAME}-${env.POM_VERSION}.${env.POM_PACKAGING} -t ${env.DOCKER_HUB}/${env.APPLICATION_NAME}:${GIT_COMMIT} ./.cicd
//             # docker build -t imagename dockerfilepath
//             echo "***********************Login to Docker Registry*******************************"
//             # docker login -u username -p password
//             docker login -u ${DOCKER_CREDS_USR} -p ${DOCKER_CREDS_PSW}
//             # docker push image_name
//             docker push ${env.DOCKER_HUB}/${env.APPLICATION_NAME}:${GIT_COMMIT}
//         """
//     }
// }


// def imageValidation(){
//     return {
//         println("Attempting to Pull the Docker Image")
//         try {
//             sh "docker pull ${env.DOCKER_HUB}/${env.APPLICATION_NAME}:${GIT_COMMIT}"
//             println("Image Pulled Succesfully!!!!")
//         }
//         catch(Exception e){
//             println("OPPS!, the docker image with this tag is not available, So creating the Image")
//             buildApp().call()
//             sonar().call()
//             dockerBuildAndPush().call()
//         }
//     }
// }

// Method for deploying containers in diff envs
// def dockerDeploy(envDeploy, hostPort, contPort){
//     return {
//         echo "Deploying to $envDeploy Environment"
//         withCredentials([usernamePassword(credentialsId: 'navya_ssh_dockerserver_creds', passwordVariable: 'PASSWORD', usernameVariable: 'USERNAME')]) {
//             script {
//                 //sshpass -p password ssh -o StrictHostKeyChecking=no username@dockerserver_ip
//                 sh "sshpass -p '$PASSWORD' -v ssh -o StrictHostKeyChecking=no $USERNAME@$dev_ip \"docker pull ${env.DOCKER_HUB}/${env.APPLICATION_NAME}:${GIT_COMMIT}\""
//                 try {
//                     // Stop container
//                     sh "sshpass -p '$PASSWORD' -v ssh -o StrictHostKeyChecking=no $USERNAME@$dev_ip docker stop ${env.APPLICATION_NAME}-$envDeploy"
//                     // Remove Container
//                     sh "sshpass -p '$PASSWORD' -v ssh -o StrictHostKeyChecking=no $USERNAME@$dev_ip docker rm ${env.APPLICATION_NAME}-$envDeploy"
//                 }
//                 catch(err) {
//                     echo "Error Caught: $err"
//                 }
//                 //Create container
//                 sh "sshpass -p '$PASSWORD' -v ssh -o StrictHostKeyChecking=no $USERNAME@$dev_ip docker run -dit --name ${env.APPLICATION_NAME}-$envDeploy -p $hostPort:$contPort ${env.DOCKER_HUB}/${env.APPLICATION_NAME}:${GIT_COMMIT}"
//             }
//         }  
//     }
// }