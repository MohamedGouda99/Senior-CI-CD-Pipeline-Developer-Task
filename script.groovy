#!/usr/bin/env groovy
def buildJar(){
     echo "building the application"
     sh './gradlew clean build'
}

def performSecurityScan() {
    echo "Running OWASP Dependency Check..."

    // Define the path for Dependency-Check installation
    def dependencyCheckHome = "${env.WORKSPACE}/dependency-check"
    def dependencyCheckBin = "${dependencyCheckHome}/dependency-check/bin/dependency-check.sh"
    def dependencyCheckZip = "${dependencyCheckHome}/dependency-check-8.4.0-release.zip"
    
    // Check if Dependency-Check is already installed
    if (!fileExists(dependencyCheckBin)) {
        echo "Dependency-Check not found. Downloading and installing..."
        
        // Create the installation directory if it doesn't exist
        sh "mkdir -p ${dependencyCheckHome}"
        
        echo "Downloading Dependency-Check..."
        sh "wget https://github.com/jeremylong/DependencyCheck/releases/download/v8.4.0/dependency-check-8.4.0-release.zip -O ${dependencyCheckZip}"
        
        echo "Unzipping Dependency-Check..."
        sh "unzip -o ${dependencyCheckZip} -d ${dependencyCheckHome}"
    } else {
        echo "Dependency-Check is already installed. Skipping download and extraction."
    }
    
    // Run the OWASP Dependency-Check using the installed version
    def exitCode = sh(script: "${dependencyCheckBin} --format XML --scan . --disableKnownExploited", returnStatus: true)
    
    if (exitCode != 0) {
        echo "OWASP Dependency-Check failed with exit code: ${exitCode}"
        error("Dependency-Check encountered an error or found vulnerabilities.")
    } else {
        echo "OWASP Dependency Check complete with no critical issues."
    }
}



// def performSecurityScan() {
//     echo "Running OWASP Dependency Check..."
    
//     // Define the path for Dependency-Check installation
//     def dependencyCheckHome = "${env.WORKSPACE}/dependency-check"
//     def reportFile = "${env.WORKSPACE}/dependency-check-report.xml"
    
//     // Clean up the previous installation, download, and unzip the latest version
//     echo "Cleaning up previous Dependency-Check installation..."
//     sh "rm -rf ${dependencyCheckHome}"
//     sh "mkdir -p ${dependencyCheckHome}"
    
//     echo "Downloading Dependency-Check..."
//     sh "wget https://github.com/jeremylong/DependencyCheck/releases/download/v8.4.0/dependency-check-8.4.0-release.zip -P ${dependencyCheckHome}"
    
//     echo "Unzipping Dependency-Check..."
//     sh "unzip -o ${dependencyCheckHome}/dependency-check-8.4.0-release.zip -d ${dependencyCheckHome}"
    
//     // Run the OWASP Dependency-Check and generate the report
//     echo "Running Dependency-Check scan..."
//     sh "${dependencyCheckHome}/dependency-check/bin/dependency-check.sh --format XML --out ${reportFile} --scan . --disableKnownExploited"
    
//     // Check the generated report for vulnerabilities
//     echo "Checking Dependency-Check report for vulnerabilities..."
//     def vulnerabilitiesFound = sh(script: "grep '<severity>' ${reportFile} | grep -i 'Critical\\|High\\|Medium'", returnStatus: true)
    
//     if (vulnerabilitiesFound == 0) {
//         error "Dependency-Check found vulnerabilities. Failing the build."
//     } else {
//         echo "No critical or high vulnerabilities found. Proceeding with the build."
//     }
    
//     echo "OWASP Dependency Check complete."
// }


def getCommitHash() {
    return sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
}


def pushImage() {
    def COMMIT_HASH = getCommitHash()
    withCredentials([usernamePassword(
        credentialsId: 'docker_hub', 
        usernameVariable: 'DOCKER_REGISTRY_USERNAME', 
        passwordVariable: 'DOCKER_REGISTRY_PASSWORD'
    )]) {
        // Authenticate with Docker Hub to push Docker image
        sh "echo \${DOCKER_REGISTRY_PASSWORD} | docker login -u \${DOCKER_REGISTRY_USERNAME} --password-stdin"
        
        // Build Docker image for app.py
        sh "docker build -t ${env.DOCKER_REGISTRY}:Build-${COMMIT_HASH}-APP ."
        
        // Push the app Docker image to Docker Hub
        sh "docker push docker.io/${env.DOCKER_REGISTRY}:Build-${COMMIT_HASH}-APP"
        sh "echo done"
        // // Remove the locally built app Docker image
        // sh "docker rmi -f ${env.DOCKER_REGISTRY}:Build-${COMMIT_HASH}-APP"
    }
    
    return COMMIT_HASH
}




def authWithAWS() {
    echo "Authenticating with AWS..."

    withCredentials([[
        $class: 'AmazonWebServicesCredentialsBinding',
        credentialsId: 'aws_credentials'
    ]]) {
        // AWS CLI commands will use the provided credentials automatically
        sh "aws sts get-caller-identity"
    }
}

def getKubeConfig() {
    echo "Retrieving kubeconfig for EKS cluster ${env.clusterName} in ${env.region}..."

    withCredentials([[
        $class: 'AmazonWebServicesCredentialsBinding',
        credentialsId: 'aws_credentials'
    ]]) {
        // Update kubeconfig using the provided credentials
        sh "aws eks update-kubeconfig --name ${env.clusterName} --region ${env.region}"
        echo "Got kubeconfig"
    }
}

return this