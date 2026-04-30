pipeline {
    agent any

    triggers {
        githubPush()
    }

    environment {
        GIT_REPO = 'https://github.com/viki-98/java-http-app.git'
        GIT_BRANCH = 'master'
        CREDENTIALS_ID = 'github-pat'

        IMAGE_NAME = 'java-http-app'
        IMAGE_TAG = 'jenkins'

        CONTAINER_NAME = 'java-http-app-jenkins'

        HOST_PORT = '8082'
        CONTAINER_PORT = '8080'
    }

    stages {
        stage('1. Checkout from GitHub') {
            steps {
                echo 'Cloning java-http-app project from GitHub...'

                git branch: "${GIT_BRANCH}",
                    credentialsId: "${CREDENTIALS_ID}",
                    url: "${GIT_REPO}"
            }
        }

        stage('2. Show project files') {
            steps {
                echo 'Checking files that Jenkins downloaded...'
                sh 'pwd'
                sh 'ls -la'
            }
        }

        stage('3. Check Dockerfile') {
            steps {
                echo 'Checking that Dockerfile exists...'
                sh 'test -f Dockerfile'
                sh 'cat Dockerfile'
            }
        }

        stage('4. Build Docker image') {
            steps {
                echo 'Building Docker image from Dockerfile...'

                sh '''
                    docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .
                '''
            }
        }

        stage('5. Remove old Jenkins test container') {
            steps {
                echo 'Removing old java-http-app-jenkins container if it exists...'

                sh '''
                    docker rm -f ${CONTAINER_NAME} || true
                '''
            }
        }

        stage('6. Run new container') {
            steps {
                echo 'Starting new container on port 8082 with database connection...'

                sh '''
                    docker run -d \
                    --name ${CONTAINER_NAME} \
                    --network tmp_app-network \
                    --restart unless-stopped \
                    -p ${HOST_PORT}:${CONTAINER_PORT} \
                    -e SERVER_PORT=8080 \
                    -e DB_HOST=postgres \
                    -e DB_PORT=5432 \
                    -e DB_NAME=appdb \
                    -e DB_USERNAME=appuser \
                    -e DB_PASSWORD=StrongPass123 \
                    ${IMAGE_NAME}:${IMAGE_TAG}
                '''
            }
        }

        stage('7. Check container') {
            steps {
                echo 'Checking if container is running...'
                sh 'docker ps --filter "name=${CONTAINER_NAME}"'
                sh 'docker logs --tail=100 ${CONTAINER_NAME}'
            }
        }
    }
}