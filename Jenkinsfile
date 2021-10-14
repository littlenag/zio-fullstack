pipeline {
    agent { label 'default' }
    options {
        ansiColor('xterm')
    }
    stages {
        stage("Git Checkout"){
            steps{
                deleteDir()
                checkout scm
            }
        }
        stage('Build') {
            steps {
                sh "sbt compile"
            }
        }
        stage('Test') {
            steps {
                sh "sbt test"
            }
        }
        stage("Package and Publish"){
           steps{
               sh "sbt rpm:publish"
           }
        }
    }
}
