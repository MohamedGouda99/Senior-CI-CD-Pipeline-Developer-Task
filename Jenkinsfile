// @Library('SharedLibrary') _
def gv
def 
pipeline{
    agent any
    tools{
       gradle 'gradle:8.1'
    }
    stages{        
        stage("init"){
            steps{
                script{
                    gv = load 'script.groovy'
                }
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