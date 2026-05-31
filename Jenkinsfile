pipeline {
    agent any

    environment {
        JUNIT_JAR_URL  = 'https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.7.1/junit-platform-console-standalone-1.7.1.jar'
        JUNIT_JAR_PATH = 'lib/junit.jar'
        CLASS_DIR      = 'classes'
        REPORT_DIR     = 'test-reports'
        // TODO: 빌드 결과 알림을 받을 이메일 주소로 변경하세요.
        NOTIFY_EMAIL   = 'your-email@example.com'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Prepare') {
            steps {
                sh '''
                    mkdir -p ${CLASS_DIR}
                    mkdir -p ${REPORT_DIR}
                    mkdir -p lib
                    echo "[+] Downloading JUnit JAR..."
                    curl -L -o ${JUNIT_JAR_PATH} ${JUNIT_JAR_URL}
                '''
            }
        }

        stage('Build') {
            steps {
                sh '''
                    echo "[+] Compiling source files..."
                    cd game
                    find src -name "*.java" > sources.txt
                    javac -encoding UTF-8 -d ../${CLASS_DIR} -cp ../${JUNIT_JAR_PATH} @sources.txt
                '''
            }
        }

        stage('Test') {
            steps {
                sh '''
                    echo "[+] Running tests with JUnit..."
                    java -jar ${JUNIT_JAR_PATH} \
                         --class-path ${CLASS_DIR} \
                         --scan-class-path \
                         --details=tree \
                         --details-theme=ascii \
                         --reports-dir ${REPORT_DIR} \
                         --config=junit.platform.output.capture.stdout=true \
                         --config=junit.platform.reporting.open.xml.enabled=true \
                         > ${REPORT_DIR}/test-output.txt
                '''
            }
        }

        stage('Deploy') {
            steps {
                echo "[+] Deploying index.html to EC2 (runs only if build + tests passed)..."
                withCredentials([sshUserPrivateKey(credentialsId: 'ec2-ssh', keyFileVariable: 'SSH_KEY', usernameVariable: 'SSH_USER')]) {
                    sh '''
                        scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -i "$SSH_KEY" \
                            index.html ${SSH_USER}@3.36.48.154:~/coming-soon/index.html
                        echo "[+] Deployed -> http://3.36.48.154:8000"
                    '''
                }
            }
        }
    }

    post {
        always {
            echo "[*] Archiving test results..."
            junit "${REPORT_DIR}/**/*.xml"
            archiveArtifacts artifacts: "${REPORT_DIR}/**/*", allowEmptyArchive: true
        }
        success {
            echo "Build and test succeeded!"
            mail to: "${NOTIFY_EMAIL}",
                 subject: "[Jenkins] SUCCESS: ${JOB_NAME} #${BUILD_NUMBER}",
                 body: "빌드/테스트/배포 성공.\n\n배포됨: http://3.36.48.154:8000\n콘솔 출력: ${BUILD_URL}console\n테스트 결과(txt): ${BUILD_URL}artifact/${REPORT_DIR}/test-output.txt"
        }
        failure {
            echo "Build or test failed!"
            mail to: "${NOTIFY_EMAIL}",
                 subject: "[Jenkins] FAILURE: ${JOB_NAME} #${BUILD_NUMBER}",
                 body: "빌드 또는 테스트 실패. 원인 분석이 필요합니다.\n\n콘솔 로그: ${BUILD_URL}console"
        }
    }
}
