pipeline {
    agent any 
    stages {
    	
        stage('ngdesk') {
			steps {
				script {

					echo 'Hello ngDesk World!' 
					sh 'ls'
					sh 'pwd'
					sh 'env'
					sh 'mvn --version'
					
					echo "Update PROD"

					// system services
					def configServerChanged = ''
											
					// backend services
					def authChanged = ''
					def dataChanged = ''
					def websocketChanged = ''
					def escalationChanged = ''
					def samChanged = ''
					def sidebarChanged = ''
					def workflowChanged = ''
					def roleChanged = ''
					def companyChanged = ''
					def moduleChanged = ''
					def graphqlChanged = ''
					def reportsChanged = ''
					def tesseractChanged = ''
					def notificationsChanged = ''
					def restChanged = ''
					def managerChanged = ''
					def gatewayChanged = ''

					// frontend services
					def nginxChanged = ''
					def uiChanged = ''
					
				
					dir('/var/jenkins_home/projects/ngdesk-project/ngDesk') {

						sh "git fetch https://github.com/SubscribeIT/ngDesk.git +refs/heads/*:refs/remotes/origin/*"

						configServerChanged = sh(returnStdout: true, script: '''git diff HEAD origin/main -- ngDesk-Config-Server ''').trim()

						authChanged = sh(returnStdout: true, script: '''git diff HEAD origin/main -- ngDesk-Auth ''').trim()
						dataChanged = sh(returnStdout: true, script: '''git diff HEAD origin/main -- ngDesk-Data-Service''').trim()
						websocketChanged = sh(returnStdout: true, script: '''git diff HEAD origin/main -- ngDesk-Websocket-Service''').trim()
						escalationChanged = sh(returnStdout: true, script: '''git diff HEAD origin/main -- ngDesk-Escalation-Service''').trim()
						samChanged = sh(returnStdout: true, script: '''git diff HEAD origin/main -- ngDesk-Sam-Service''').trim()
						sidebarChanged = sh(returnStdout: true, script: '''git diff HEAD origin/main -- ngDesk-Sidebar-Service''').trim()
						workflowChanged = sh(returnStdout: true, script: '''git diff HEAD origin/main -- ngDesk-Workflow-Service''').trim()
						roleChanged = sh(returnStdout: true, script: '''git diff HEAD origin/main -- ngDesk-Role-Service''').trim()
						companyChanged = sh(returnStdout: true, script: '''git diff HEAD origin/main -- ngDesk-Company-Service''').trim()
						moduleChanged = sh(returnStdout: true, script: '''git diff HEAD origin/main -- ngDesk-Module-Service''').trim()
						graphqlChanged = sh(returnStdout: true, script: '''git diff HEAD origin/main -- ngDesk-Graphql ''').trim()
						reportsChanged = sh(returnStdout: true, script: '''git diff HEAD origin/main -- ngDesk-Report-Service ''').trim()
						tesseractChanged = sh(returnStdout: true, script: '''git diff HEAD origin/main -- ngDesk-Tesseract-Service ''').trim()
						notificationsChanged = sh(returnStdout: true, script: '''git diff HEAD origin/main -- ngDesk-Notification-Service ''').trim()
						restChanged = sh(returnStdout: true, script: '''git diff HEAD origin/main -- ngDesk-Rest ''').trim()
						managerChanged = sh(returnStdout: true, script: '''git diff HEAD origin/main -- ngDesk-Manager ''').trim()
						gatewayChanged = sh(returnStdout: true, script: '''git diff HEAD origin/main -- ngDesk-Gateway ''').trim()


						nginxChanged = sh(returnStdout: true, script: '''git diff HEAD origin/main -- ngDesk-Nginx''').trim()
						uiChanged = sh(returnStdout: true, script: '''git diff HEAD origin/main -- ngDesk-UI''').trim()
						


						checkout([$class: 'GitSCM', branches: [[name: 'origin/main']], userRemoteConfigs: [[url: 'https://github.com/SubscribeIT/ngDesk.git']]])
					}

					if (configServerChanged.length() > 0) {
						buildMicroservice('config-server', 'ngDesk-Config-Server')
					}

					if (authChanged.length() > 0) {
						buildMicroservice('auth', 'ngDesk-Auth')
					}
					
					if(uiChanged.length() > 0){
						dir('/var/jenkins_home/projects/ngdesk-web-project') {
							checkout([$class: 'GitSCM', branches: [[name: '*/main']], userRemoteConfigs: [[credentialsId: "${env.GIT_CREDENTIAL_ID}", url: "${env.GIT_WEB_URL}"]]])
						}
					}

					if (graphqlChanged.length() > 0) {
						buildMicroservice('graphql', 'ngDesk-Graphql')
					}

					if (samChanged.length() > 0) {
						buildMicroservice('sam', 'ngDesk-Sam-Service')
					}

					if (reportsChanged.length() > 0) {
						buildMicroservice('report', 'ngDesk-Report-Service')
					}

					if (notificationsChanged.length() > 0) {
						buildMicroservice('notification', 'ngDesk-Notification-Service')
					}
			
					if (companyChanged.length() > 0) {
						buildMicroservice('company', 'ngDesk-Company-Service')
					}

					if (moduleChanged.length() > 0) {
						buildMicroservice('module', 'ngDesk-Module-Service')
					}

					if (dataChanged.length() > 0) {
						buildMicroservice('data', 'ngDesk-Data-Service')
					}

					if (websocketChanged.length() > 0) {
						buildMicroservice('websocket', 'ngDesk-Websocket-Service')
					}

					if (escalationChanged.length() > 0) {
						buildMicroservice('escalation', 'ngDesk-Escalation-Service')
					}

					if (sidebarChanged.length() > 0) {
						buildMicroservice('sidebar', 'ngDesk-Sidebar-Service')
					}

					if (workflowChanged.length() > 0) {
						buildMicroservice('workflow', 'ngDesk-Workflow-Service')
					}

					if (roleChanged.length() > 0) {
						buildMicroservice('role', 'ngDesk-Role-Service')
					}

					if (restChanged.length() > 0) {
						buildMicroservice('rest', 'ngDesk-Rest')
					}

					if (managerChanged.length() > 0) {
						buildMicroservice('manager', 'ngDesk-Manager')
					}

					if (gatewayChanged.length() > 0) {
						buildMicroservice('gateway', 'ngDesk-Gateway')
					}

					if (nginxChanged.length() > 0) {
						dir('/var/jenkins_home/projects/ngdesk-project/ngDesk/ngDesk-Nginx') {
							docker.withRegistry("${env.DOCKER_HUB_URL}", "${env.DOCKER_HUB_KEY}") {
								def newImage = docker.build("${env.DOCKER_IMAGE_NAME}/nginx:latest")
								newImage.push()
							}
						}
					}
					
					if (uiChanged.length() > 0) {	
						generateSwagger('ngDesk-UI', '../../ngDesk-Private/ngDesk-Payment-Service/target/openapi.json', 'payment-api')
						generateSwagger('ngDesk-UI', '../../ngDesk-Private/ngDesk-Integration-Service/target/openapi.json', 'integration-api')
						generateSwagger('ngDesk-UI', '../ngDesk-Auth/target/openapi.json', 'auth-api')
						generateSwagger('ngDesk-UI', '../ngDesk-Workflow-Service/target/openapi.json', 'workflow-api')
						generateSwagger('ngDesk-UI', '../ngDesk-Module-Service/target/openapi.json', 'module-api')
						generateSwagger('ngDesk-UI', '../ngDesk-Role-Service/target/openapi.json', 'role-api')
						generateSwagger('ngDesk-UI', '../ngDesk-Escalation-Service/target/openapi.json', 'escalation-api')
						generateSwagger('ngDesk-UI', '../ngDesk-Sam-Service/target/openapi.json', 'sam-api')
						generateSwagger('ngDesk-UI', '../ngDesk-Sidebar-Service/target/openapi.json', 'sidebar-api')
						generateSwagger('ngDesk-UI', '../ngDesk-Data-Service/target/openapi.json', 'data-api')
						generateSwagger('ngDesk-UI', '../ngDesk-Report-Service/target/openapi.json', 'report-api')
						generateSwagger('ngDesk-UI', '../ngDesk-Notification-Service/target/openapi.json', 'notification-api')
						generateSwagger('ngDesk-UI', '../ngDesk-Company-Service/target/openapi.json', 'company-api')


						dir('/var/jenkins_home/projects/ngdesk-project/ngDesk/ngDesk-UI') {
							sh 'rm -f package-lock.json'
							// sh 'rm -rf node_modules'
							sh 'rm -rf node_modules/@ngdesk'
							sh 'npm install --unsafe-perm --ignore-scripts'
							// sh '/opt/sonar-scanner-4.3.0.2102-linux/bin/sonar-scanner'
							sh 'ng build -c=prd'
							sh 'npm run post-build'
							sh 'cp -r dist/ngDesk-Angular/. /var/jenkins_home/projects/ngdesk-web-project/src/main/resources/static/'
						}

							dir('/var/jenkins_home/projects/ngdesk-web-project') {
							sh 'mvn package'
							sh './mvnw spring-boot:build-image'

								docker.withRegistry("${env.DOCKER_HUB_URL}", "${env.DOCKER_HUB_KEY}") {
									def newImage = docker.image("${env.DOCKER_IMAGE_NAME}/web:latest")
									newImage.push()
									docker.withServer("${env.PROD_SERVER_URL}") {
										sh "docker rename ngdesk-web ngdesk-web-old"
										sh "docker stop ngdesk-web-old"
										sh "docker pull ngdesk/web"
										sh "docker run --name ngdesk-web -d -e SPRING_PROFILES_ACTIVE=dockernew --network=host ngdesk/web"
										sh "docker rm ngdesk-web-old"
										sh 'docker image prune -f'
									}
								} 
							}

					}
				}
				emailext (
					subject: "PROD Deployment success!!!",
					body: "Go check PROD",
					to: "${EMPLOYEE_EMAIL_ADDRESSES}",
					from: "${JENKINS_FROM_EMAIL_ADDRESS}"
				)
			}
		}
	}

	post {
    	always {

      		echo 'stuff to do always'   
      		sh 'env'

    	}
		failure {
			script {
				echo 'pipeline failed, at least one step failed'

				emailext (
					subject: "PROD FAILED deployment!!!",
					body: "Go check Jenkins and PROD",
					to: "${EMPLOYEE_EMAIL_ADDRESSES}",
					from: "${JENKINS_FROM_EMAIL_ADDRESS}"
				)
			}

    	}
  	}

}
def buildMicroservice(serviceName, path) {
 dir('/var/jenkins_home/projects/ngdesk-project/ngDesk/' + path) {


	 if(serviceName == 'rest' || serviceName == 'manager' || serviceName == 'gateway' || serviceName == 'config-server'){
		 sh 'mvn package -DskipTests'

		 if(serviceName == 'gateway' || serviceName == 'config-server'){
			 sh './mvnw spring-boot:build-image'
		 } else {
			 docker.build("${env.DOCKER_IMAGE_NAME}/" + serviceName + ":latest")
		 }
	 } else{
        sh 'mvn install -f pom-packaging.xml'

        // Generate package
        sh 'mvn package -f pom-packaging.xml'

        // Generate swagger
        sh 'mvn verify -f pom-packaging.xml'

        // Run unit test
        sh 'mvn test -f pom-packaging.xml'
        //junit '**/surefire-reports/*.xml'
        
        
        sh "mvn sonar:sonar -Dsonar.projectKey=${path} -Dsonar.host.url=${env.SONAR_URL} -Dsonar.login=${env.SONAR_LOGIN}"
        
        sh './mvnw spring-boot:build-image'
	 }
        docker.withRegistry("${env.DOCKER_HUB_URL}", "${env.DOCKER_HUB_KEY}") {
            def newImage = docker.image("${env.DOCKER_IMAGE_NAME}/" + serviceName + ":latest")
            newImage.push()
            if(serviceName != 'config-server'){
				docker.withServer("${env.PROD_SERVER_URL}") {
					sh "docker rename ngdesk-${serviceName} ngdesk-${serviceName}-old"
					sh "docker stop ngdesk-${serviceName}-old"
					sh "docker pull ngdesk/${serviceName}"
					sh "docker run --mount type=bind,source=/opt/ngdesk,target=/opt/ngdesk --name ngdesk-${serviceName} -d -e SPRING_PROFILES_ACTIVE=dockernew --network=host ngdesk/${serviceName}"
					sh "docker rm ngdesk-${serviceName}-old"
					sh 'docker image prune -f'
				}
            }
         }
        
       
        
    }
}

def generateSwagger(frontendProject, serviceJsonPath, name) {

    dir('/var/jenkins_home/projects/ngdesk-project/ngDesk/' + frontendProject) {
        sh "openapi-generator-cli generate -g typescript-angular -i ${serviceJsonPath} -o ngdesk-swagger/${name} --additional-properties npmName=@ngdesk/${name},ngVersion=11.0.0,npmVersion=1.0.0"
        dir('ngdesk-swagger/' + name) {
            sh 'npm install'
            sh 'npm run build'
            dir('dist') {
                sh 'npm pack'
            }
        }
    }

}


