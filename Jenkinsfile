pipeline {
    agent any

    tools {
        maven 'Maven'
    }

    environment {
        SONAR_TOKEN = credentials('SONAR_TOKEN')
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/Sravanthi1206/LoanManagementSystemBackend.git'
            }
        }

        stage('Build, Test & SonarCloud') {
            steps {
                bat """
                mvn clean verify ^
                org.sonarsource.scanner.maven:sonar-maven-plugin:sonar ^
                -Dsonar.token=%SONAR_TOKEN% ^
                -Dsonar.host.url=https://sonarcloud.io ^
                -Dsonar.coverage.jacoco.xmlReportPaths=**/target/site/jacoco/jacoco.xml ^
                -Dsonar.qualitygate.wait=true
                """
            }
        }

        stage('Build Docker Images') {
            steps {
                bat 'docker-compose build'
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed.'
        }
    }
}
