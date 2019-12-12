def call (Map pipelineParams){
pipeline{
options {
    disableConcurrentBuilds()
    skipDefaultCheckout()
  }
  environment {
  def committerEmail = "test@transplace.com"
  }
agent any
	stages{
		stage('git clone'){
			steps{
			  	script {
				//git credentialsId: 'Bitbucket', url: 'https://NagarajuRapelli@bitbucket.org/NagarajuRapelli/mbp.git'
				stageName = "${STAGE_NAME}"
				echo "We are in git clone"	
					checkout scm	
					
			  }
		    }
		}
	} // Stages
	post {
	always{
		script {
			echo "${committerEmail}"
			echo "Calling sub function"
			testResults(true)
			echo "$pipelineParams.buildCommand"
			echo "${BRANCH_NAME}"
			def changeLogSets = currentBuild.changeSets
				for (int i = 0; i < changeLogSets.size(); i++) {
				    def entries = changeLogSets[i].items
				    for (int j = 0; j < entries.length; j++) {
					def entry = entries[j]
					echo "${entry.commitId} by ${entry.author} on ${new Date(entry.timestamp)}: ${entry.msg}"
					def files = new ArrayList(entry.affectedFiles)
					for (int k = 0; k < files.size(); k++) {
					    def file = files[k]
					    echo "  ${file.editType.name} ${file.path}"
					}
				    }
				}
		}
		}
	}
} // Pipeline
	
}


