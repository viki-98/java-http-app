pipeline {
    agent any

    triggers {
        githubPush()
    }

    options {
        disableConcurrentBuilds()
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 20, unit: 'MINUTES')
    }

    parameters {
        string(
            name: 'GIT_REPO_URL',
            defaultValue: 'https://github.com/viki-98/java-http-app.git',
            description: 'GitHub repository URL'
        )

        string(
            name: 'GIT_BRANCH',
            defaultValue: 'master',
            description: 'Git branch to build'
        )

        string(
            name: 'IMAGE_NAME',
            defaultValue: 'java-http-app',
            description: 'Docker image name'
        )

        string(
            name: 'IMAGE_TAG',
            defaultValue: 'jenkins',
            description: 'Docker image tag'
        )

        string(
            name: 'CONTAINER_NAME',
            defaultValue: 'java-http-app-jenkins',
            description: 'Docker container name'
        )

        string(
            name: 'HOST_PORT',
            defaultValue: '8082',
            description: 'External port on the server'
        )

        string(
            name: 'CONTAINER_PORT',
            defaultValue: '8080',
            description: 'Internal port inside Docker container'
        )
    }

    environment {
        CREDENTIALS_ID = 'github-pat'

        DOCKER_NETWORK = 'tmp_app-network'

        DB_HOST = 'postgres'
        DB_PORT = '5432'
        DB_NAME = 'appdb'
        DB_USERNAME = 'appuser'
    }

    stages {
        stage('1. Checkout from GitHub') {
            steps {
                echo "Cloning repository: ${params.GIT_REPO_URL}"
                echo "Branch: ${params.GIT_BRANCH}"

                git branch: "${params.GIT_BRANCH}",
                    credentialsId: "${env.CREDENTIALS_ID}",
                    url: "${params.GIT_REPO_URL}"
            }
        }

        stage('2. Show project files') {
            steps {
                echo 'Checking files downloaded by Jenkins...'

                sh '''
                    echo "Current workspace:"
                    pwd

                    echo "Project files:"
                    ls -la
                '''
            }
        }

        stage('3. Validate Docker configuration') {
            steps {
                echo 'Checking Dockerfile and Docker network...'

                sh '''
                    echo "Checking Dockerfile..."
                    test -f Dockerfile

                    echo "Dockerfile exists."

                    echo "Checking Docker network..."
                    docker network inspect ${DOCKER_NETWORK} > /dev/null

                    echo "Docker network ${DOCKER_NETWORK} exists."
                '''
            }
        }

        stage('4. Build Docker image') {
            steps {
                echo "Building Docker image: ${params.IMAGE_NAME}:${params.IMAGE_TAG}"

                sh '''
                    docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .
                '''
            }
        }

        stage('5. Remove old container') {
            steps {
                echo "Removing old container if exists: ${params.CONTAINER_NAME}"

                sh '''
                    docker rm -f ${CONTAINER_NAME} || true
                '''
            }
        }

        stage('6. Run new container') {
            steps {
                echo "Running new container: ${params.CONTAINER_NAME}"
                echo "Port mapping: ${params.HOST_PORT}:${params.CONTAINER_PORT}"

                withCredentials([
                    string(credentialsId: 'db-password', variable: 'DB_PASSWORD_VALUE')
                ]) {
                    sh '''
                        docker run -d \
                        --name ${CONTAINER_NAME} \
                        --network ${DOCKER_NETWORK} \
                        --restart unless-stopped \
                        -p ${HOST_PORT}:${CONTAINER_PORT} \
                        -e SERVER_PORT=${CONTAINER_PORT} \
                        -e DB_HOST=${DB_HOST} \
                        -e DB_PORT=${DB_PORT} \
                        -e DB_NAME=${DB_NAME} \
                        -e DB_USERNAME=${DB_USERNAME} \
                        -e DB_PASSWORD="${DB_PASSWORD_VALUE}" \
                        ${IMAGE_NAME}:${IMAGE_TAG}
                    '''
                }
            }
        }

        stage('7. Check container') {
            steps {
                echo "Checking container: ${params.CONTAINER_NAME}"

                sh '''
                    echo "Running container:"
                    docker ps --filter "name=${CONTAINER_NAME}"

                    echo "Container logs:"
                    docker logs --tail=100 ${CONTAINER_NAME}
                '''
            }
        }

        stage('8. Health check') {
            steps {
                echo "Checking application health on localhost:${params.HOST_PORT}"

                sh '''
                    sleep 5

                    echo "Health check:"
                    curl -f http://localhost:${HOST_PORT}/actuator/health || true
                '''
            }
        }
    }

    post {
        success {
            echo 'Pipeline finished successfully. Application was built and deployed.'
        }

        failure {
            echo 'Pipeline failed. Check Console Output and container logs.'
        }

        always {
            echo 'Pipeline finished.'
        }
    }
}