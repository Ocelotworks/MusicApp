pipeline {
  agent any
  stages {
    stage('Compile') {
      steps {
        sh 'gradle compileDebugSources'
      }
    }
    stage('Build') {
      steps {
        sh 'gradle assembleDebug'
        archiveArtifacts(artifacts: '**/*.apk', onlyIfSuccessful: true)
      }
    }
  }
}