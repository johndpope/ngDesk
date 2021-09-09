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
                    
                    echo "Update PROD"

                                              

                        // backend services
                        def authChanged = ''
                        def integrationChanged = ''
                        def paymentChanged = ''
                        def dataChanged = ''
                        def websocketChanged = ''
                        def escalationChanged = ''
                        def samChanged = ''
                        def sidebarChanged = ''
                        def workflowChanged = ''
                        def roleChanged = ''
                        def companyChanged = ''
                        def moduleChanged = ''
                        def pluginChanged = ''
                        def graphqlChanged = ''
                        def reportsChanged = ''
                        def tesseractChanged = ''
                        def notificationsChanged = ''

                        // frontend services
                        def uiChanged = ''
                        def landingPageChanged = ''
                        def outlookPluginChanged = ''
                        def microsoftPluginChanged = ''
                        def formsWidgetChanged = ''

                        // probes
                        def softwareProbeChanged = ''
                        def assetProbeChanged = ''
                        def patchProbeChanged = ''
                        def controllerChanged = ''
                    
                    
                     dir('/root/projects/ngdesk-project') {

                                sh "git fetch https://github.com/SubscribeIT/ngDesk.git +refs/heads/*:refs/remotes/origin/*"

                                authChanged = sh(returnStdout: true, script: '''git diff HEAD origin/main -- ngDesk-Auth ''').trim()
                                integrationChanged = sh(returnStdout: true, script: '''git diff HEAD origin/main -- ngDesk-Integration-Service ''').trim()
                                paymentChanged = sh(returnStdout: true, script: '''git diff HEAD origin/main -- ngDesk-Payment-Service ''').trim()
                                dataChanged = sh(returnStdout: true, script: '''git diff HEAD origin/main -- ngDesk-Data-Service''').trim()
                                websocketChanged = sh(returnStdout: true, script: '''git diff HEAD origin/main -- ngDesk-Websocket-Service''').trim()
                                escalationChanged = sh(returnStdout: true, script: '''git diff HEAD origin/main -- ngDesk-Escalation-Service''').trim()
                                samChanged = sh(returnStdout: true, script: '''git diff HEAD origin/main -- ngDesk-Sam-Service''').trim()
                                sidebarChanged = sh(returnStdout: true, script: '''git diff HEAD origin/main -- ngDesk-Sidebar-Service''').trim()
                                workflowChanged = sh(returnStdout: true, script: '''git diff HEAD origin/main -- ngDesk-Workflow-Service''').trim()
                                roleChanged = sh(returnStdout: true, script: '''git diff HEAD origin/main -- ngDesk-Role-Service''').trim()
                                companyChanged = sh(returnStdout: true, script: '''git diff HEAD origin/main -- ngDesk-Company-Service''').trim()
                                moduleChanged = sh(returnStdout: true, script: '''git diff HEAD origin/main -- ngDesk-Module-Service''').trim()
                                pluginChanged = sh(returnStdout: true, script: '''git diff HEAD origin/main -- ngDesk-Plugin-Service''').trim()
                                graphqlChanged = sh(returnStdout: true, script: '''git diff HEAD origin/main -- ngDesk-Graphql ''').trim()
                                reportsChanged = sh(returnStdout: true, script: '''git diff HEAD origin/main -- ngDesk-Report-Service ''').trim()
                                tesseractChanged = sh(returnStdout: true, script: '''git diff HEAD origin/main -- ngDesk-Tesseract-Service ''').trim()
                                notificationsChanged = sh(returnStdout: true, script: '''git diff HEAD origin/main -- ngDesk-Notification-Service ''').trim()

                                uiChanged = sh(returnStdout: true, script: '''git diff HEAD origin/main -- ngDesk-UI''').trim()
                                


                            }


                              checkout([$class: 'GitSCM', branches: [[name: 'origin/main']], userRemoteConfigs: [[url: 'https://github.com/SubscribeIT/ngDesk.git']]])

                        }
		echo "${authChanged}"
                        echo "${integrationChanged}"
                        echo "${paymentChanged}"
                        echo "${dataChanged}"
                        echo "${websocketChanged}"
                        echo "${escalationChanged}"
                        echo "${samChanged}"
                        echo "${sidebarChanged}"
                        echo "${workflowChanged}"
                        echo "${roleChanged}"
                        echo "${companyChanged}"
                        echo "${moduleChanged}"
                        echo "${pluginChanged}"
                        echo "${graphqlChanged}"
                        echo "${reportsChanged}"
                        echo "${tesseractChanged}"
                        echo "${notificationsChanged}"

                        // frontend services
                        echo "${uiChanged}"

                

}
}
}
}
}










