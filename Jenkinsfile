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
                    
                    echo $payload

                    def gitlabBranch = env.gitlabBranch.toLowerCase()
                    def gitSlug = env.gitlabSourceRepoName.toLowerCase()
                    def serviceName = env.gitlabSourceRepoName.toLowerCase().replaceAll("ngdesk", "").replaceAll("service", "").replaceAll("-", "")
                    def gitlabSourceBranch = env.gitlabSourceBranch
                    def gitlabTargetBranch = env.gitlabTargetBranch
                    def gitlabUserEmail = env.gitlabUserEmail

                    echo "gitlabBranch: ${gitlabBranch}"
                    echo "gitSlug: ${gitSlug}"
                    echo "serviceName: ${serviceName}"
                    echo "gitlabSourceBranch: ${gitlabSourceBranch}"
                    echo "gitlabTargetBranch: ${gitlabTargetBranch}"
                    echo "gitlabUserEmail: ${gitlabUserEmail}"


                

}
}
}
}
}










