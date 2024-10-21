#!/usr/bin/env groovy
def buildJar(){
     echo "building the application"
     sh './gradlew clean build'
}




def performSecurityScan() {
    echo "Running OWASP Dependency Check..."
    
    // Use the Dependency-Check plugin in Jenkins
    dependencyCheck additionalArguments: '--format XML', odcInstallation: 'DependencyCheck'
    
    echo "OWASP Dependency Check completed."
}



def getCommitHash() {
    return sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
}


def pushImage() {
    def COMMIT_HASH = getCommitHash()
    withCredentials([usernamePassword(
        credentialsId: 'docker', 
        usernameVariable: 'DOCKER_REGISTRY_USERNAME', 
        passwordVariable: 'DOCKER_REGISTRY_PASSWORD'
    )]) {
        // Authenticate with Docker Hub to push Docker image
        sh "echo \${DOCKER_REGISTRY_PASSWORD} | docker login -u \${DOCKER_REGISTRY_USERNAME} --password-stdin"
        
        // Build Docker image for app.py
        sh "docker build -t ${env.DOCKER_REGISTRY}:Build-${COMMIT_HASH}-APP ."
        
        // Push the app Docker image to Docker Hub
        sh "docker push docker.io/${env.DOCKER_REGISTRY}:Build-${COMMIT_HASH}-APP"
        
        // // Remove the locally built app Docker image
        // sh "docker rmi -f ${env.DOCKER_REGISTRY}:Build-${COMMIT_HASH}-APP"
    }
    
    return COMMIT_HASH
}



return this