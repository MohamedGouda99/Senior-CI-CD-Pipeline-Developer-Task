#!/usr/bin/env groovy
def buildJar(){
     echo "building the application"
     sh './gradlew clean build'
}




def performSecurityScan() {
    echo "Running OWASP Dependency Check..."
    
    // Define the path for Dependency-Check installation
    def dependencyCheckHome = "${env.WORKSPACE}/dependency-check"
    def reportFile = "${env.WORKSPACE}/dependency-check-report.xml"
    
    // Clean up the previous installation, download, and unzip the latest version
    echo "Cleaning up previous Dependency-Check installation..."
    sh "rm -rf ${dependencyCheckHome}"
    sh "mkdir -p ${dependencyCheckHome}"
    
    echo "Downloading Dependency-Check..."
    sh "wget https://github.com/jeremylong/DependencyCheck/releases/download/v8.4.0/dependency-check-8.4.0-release.zip -P ${dependencyCheckHome}"
    
    echo "Unzipping Dependency-Check..."
    sh "unzip -o ${dependencyCheckHome}/dependency-check-8.4.0-release.zip -d ${dependencyCheckHome}"
    
    // Run the OWASP Dependency-Check and generate the report
    echo "Running Dependency-Check scan..."
    sh "${dependencyCheckHome}/dependency-check/bin/dependency-check.sh --format XML --out ${reportFile} --scan ."
    
    // Check the generated report for vulnerabilities
    echo "Checking Dependency-Check report for vulnerabilities..."
    def vulnerabilitiesFound = sh(script: "grep '<severity>' ${reportFile} | grep -i 'Critical\\|High\\|Medium'", returnStatus: true)
    
    if (vulnerabilitiesFound == 0) {
        error "Dependency-Check found vulnerabilities. Failing the build."
    } else {
        echo "No critical or high vulnerabilities found. Proceeding with the build."
    }
    
    echo "OWASP Dependency Check complete."
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