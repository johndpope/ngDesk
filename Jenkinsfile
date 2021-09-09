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
				                            

				        // backend services
				        def authChanged = ''
				        def integrationChanged = ''

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

				        // frontend services
				        def uiChanged = ''
				    
				    
				     dir('/var/jenkins_home/projects/ngdesk-project/ngDesk') {

				                sh "git fetch https://github.com/SubscribeIT/ngDesk.git +refs/heads/*:refs/remotes/origin/*"

				                authChanged = sh(returnStdout: true, script: '''git diff HEAD origin/main -- ngDesk-Auth ''').trim()
				                integrationChanged = sh(returnStdout: true, script: '''git diff HEAD origin/main -- ngDesk-Integration-Service ''').trim()
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

				                uiChanged = sh(returnStdout: true, script: '''git diff HEAD origin/main -- ngDesk-UI''').trim()
				              checkout([$class: 'GitSCM', branches: [[name: 'origin/main']], userRemoteConfigs: [[url: 'https://github.com/SubscribeIT/ngDesk.git']]])
				            }

					if (authChanged.length() > 0) {
                            			buildMicroservice('auth', 'ngDesk-Auth')
                        		}

				        if (integrationChanged.length() > 0) {
                           			 buildMicroservice('integration', 'ngDesk-Integration-Service')
				        }

				        if (graphqlChanged.length() > 0) {
				            buildMicroservice('graphql', 'ngDesk-Graphql')
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

				

					}
				}
			}
		}
}
def buildMicroservice(serviceName, path) {
 dir('/var/jenkins_home/projects/ngdesk-project/ngDesk' + path) {

        sh 'mvn install -f pom-packaging.xml'

        // Generate package
        sh 'mvn package -f pom-packaging.xml'

        // Generate swagger
        sh 'mvn verify -f pom-packaging.xml'

        // Run unit test
        sh 'mvn test -f pom-packaging.xml'
        //junit '**/surefire-reports/*.xml'
        
        //create docker image
        // push to docker hub
        // post to prod
        
    }
}

def generateSwagger(frontendProject, serviceJsonPath, name) {

    dir('/var/jenkins_home/projects/ngdesk-project/ngDesk' + frontendProject) {

        sh "openapi-generator generate -g typescript-angular -i ${serviceJsonPath} -o ngdesk-swagger/${name} --additional-properties npmName=@ngdesk/${name},ngVersion=11.0.0,npmVersion=1.0.0"
        dir('ngdesk-swagger/' + name) {
            sh 'npm install'
            sh 'npm run build'
            dir('dist') {
                sh 'npm pack'
            }
        }
    }

}









