pipeline {
    agent any

    tools {
        maven 'Maven'
    }

    environment {
        SONAR_TOKEN = credentials('SONAR_TOKEN')
        ENV_FILE = credentials('ENV_FILE')
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
                -Dsonar.organization=sravanthi1206 ^
                -Dsonar.projectKey=Sravanthi1206_LoanManagementSystemBackend ^
                -Dsonar.coverage.jacoco.xmlReportPaths=**/target/site/jacoco/jacoco.xml ^
                -Dsonar.qualitygate.wait=true
                """
            }
        }

        stage('Setup Environment') {
            steps {
                bat 'copy %ENV_FILE% .env'
            }
        }

        stage('Docker Compose Up') {
            steps {
                bat 'docker compose up -d --build'
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
        always {
            bat 'if exist .env del .env'
        }
    }
}
