pipeline {
  agent any
  stages {
    stage('Compile') {
      steps {
        sh './gradlew compileDebugSources'
      }
    }
    stage('Build') {
      steps {
        sh './gradlew assembleDebug'
        archiveArtifacts(artifacts: '**/*.apk', onlyIfSuccessful: true)
      }
    }
  }
}