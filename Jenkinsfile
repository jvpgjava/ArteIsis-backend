// ArteIsis backend — mesmo padrão Flowtix: deploy local na VPS (sem scp/ssh)
// Branch master → prod | branch hml → homologação
// Repo: https://github.com/jvpgjava/ArteIsis-backend.git

pipeline {
    agent any

    environment {
        DEPLOY_USER = 'jgrando'
        JAR_NAME    = 'arte-isis-api-0.0.1-SNAPSHOT.jar'
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 15, unit: 'MINUTES')
        disableConcurrentBuilds()
    }

    stages {
        stage('Set Environment') {
            steps {
                script {
                    def branchName = env.BRANCH_NAME ?: env.GIT_BRANCH?.replaceAll('origin/', '') ?: ''
                    if (branchName == 'master') {
                        env.PROFILE      = 'prod'
                        env.DEPLOY_DIR   = '/var/www/arteisis/prod/backend'
                        env.SERVICE_NAME = 'arteisis-prod'
                        env.ENV_LABEL    = 'PRODUÇÃO'
                    } else if (branchName == 'hml') {
                        env.PROFILE      = 'hml'
                        env.DEPLOY_DIR   = '/var/www/arteisis/hml/backend'
                        env.SERVICE_NAME = 'arteisis-hml'
                        env.ENV_LABEL    = 'HOMOLOG'
                    } else {
                        env.PROFILE   = ''
                        env.ENV_LABEL = 'N/A'
                    }
                }
                echo "Branch: ${env.BRANCH_NAME} → Ambiente: ${env.ENV_LABEL}"
            }
        }

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests -B'
            }
        }

        stage('Deploy') {
            when {
                expression { env.PROFILE != '' }
            }
            steps {
                script {
                    def jarPath = "target/${env.JAR_NAME}"
                    if (!fileExists(jarPath)) {
                        error "JAR não encontrado: ${jarPath}"
                    }
                    sh """
                        sudo mkdir -p ${env.DEPLOY_DIR}
                        sudo cp ${jarPath} ${env.DEPLOY_DIR}/arteisis.jar
                        sudo chown ${DEPLOY_USER}:${DEPLOY_USER} ${env.DEPLOY_DIR}/arteisis.jar
                        sudo systemctl restart ${env.SERVICE_NAME}
                    """
                }
            }
        }

        stage('Health Check') {
            when {
                expression { env.PROFILE != '' }
            }
            steps {
                script {
                    sleep 10
                    def status = sh(
                        script: "sudo systemctl is-active ${env.SERVICE_NAME}",
                        returnStdout: true
                    ).trim()
                    if (status != 'active') {
                        error "Service ${env.SERVICE_NAME} não está ativo: ${status}"
                    }
                }
                echo "Service ${env.SERVICE_NAME} is active."
            }
        }
    }

    post {
        success {
            echo "ArteIsis backend [${env.ENV_LABEL}] concluído com sucesso."
        }
        failure {
            echo "ArteIsis backend [${env.ENV_LABEL}] falhou. Verifique os logs."
        }
        always {
            cleanWs(deleteDirs: true, notFailBuild: true)
        }
    }
}
