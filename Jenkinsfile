pipeline {
  agent { dockerfile true }

    stages {
        stage('Compile') {
            steps {
                echo "Compiling..."
                sh "sbt compile"
            }
        }
        stage('test') {
            steps {
                echo "Testing..."
                sh "sbt test"
            }
        }
    }
}
