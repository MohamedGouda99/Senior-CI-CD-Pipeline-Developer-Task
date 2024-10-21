// @Library('SharedLibrary') _
def gv

pipeline{
    agent any
   
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
                // Checkout the code from the repository
                git 'https://github.com/MohamedGouda99/Senior-CI-CD-Pipeline-Developer-Task.git'
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