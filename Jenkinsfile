// @Library('SharedLibrary') _
def gv

pipeline{
    agent any
    environment {
        // Set the Gradle Home if needed
        GRADLE_HOME = "${WORKSPACE}/gradlew"
        // Replace 'github-credentials-id' with the actual ID of your Jenkins GitHub credentials
        GITHUB_CREDENTIALS_ID = 'github_creds'
    }
    stages{        
        stage("init"){
            steps{
                script{
                    gv = load 'script.groovy'
                }
            }
        }

        stage('Checkout') {
            steps {
                // Checkout the code from the repository using credentials
                git credentialsId: "${GITHUB_CREDENTIALS_ID}", branch: 'main', url: 'https://github.com/MohamedGouda99/Senior-CI-CD-Pipeline-Developer-Task.git'
            }
        }

        stage("build jar"){
            steps{
                script{
                   gv.buildJar()

                }
            }
        }
        

        stage("OWASP Scanning"){
            steps{
                script{
                        gv.performSecurityScan()

                    }
                }
        }


        stage("build and image") {
            steps {
                script {
                    gv.pushImage()
                }
            }
        }
        
        
        // stage("deploying"){
        //     steps{
        //         script{
        //           gv.deploy()
        //         }
        //     }
        // }
    }
}       