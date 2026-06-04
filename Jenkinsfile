// ArteIsis backend — branch master → prod | branch hml → homologação
// Repo: https://github.com/jvpgjava/ArteIsis-backend.git

def deployEnv = (env.BRANCH_NAME == 'hml') ? 'hml' : 'prod'
def deployDir = deployEnv == 'prod' ? '/var/www/arteisis/prod/backend' : '/var/www/arteisis/hml/backend'
def systemdUnit = deployEnv == 'prod' ? 'arteisis-prod' : 'arteisis-hml'
def healthUrl = deployEnv == 'prod'
    ? 'https://api-arteisis.jgnx.com.br/api/catalog/products'
    : 'https://api-hml-arteisis.jgnx.com.br/api/catalog/products'

pipeline {
    agent any

    environment {
        SSH_HOST = '72.61.47.148'
        SSH_USER = 'jgrando'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build JAR') {
            steps {
                echo "Branch: ${env.BRANCH_NAME} → ambiente: ${deployEnv}"
                sh 'mvn -B clean package -DskipTests'
            }
        }

        stage('Deploy') {
            steps {
                sh """
                    JAR=\$(ls target/arte-isis-api-*.jar | grep -v '.original' | head -1)
                    test -n "\$JAR" || { echo 'JAR não encontrado em target/'; exit 1; }
                    scp -o StrictHostKeyChecking=accept-new "\$JAR" ${SSH_USER}@${SSH_HOST}:${deployDir}/arteisis.jar
                    ssh -o StrictHostKeyChecking=accept-new ${SSH_USER}@${SSH_HOST} "sudo systemctl restart ${systemdUnit}"
                """
            }
        }

        stage('Health') {
            steps {
                sh "curl -fsS '${healthUrl}' || true"
            }
        }
    }
}
