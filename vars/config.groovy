// vars/config.groovy

class config implements Serializable {
	//dockerhub
	def dockerhubUrl = "dockerhub-app-01.east1e.nonprod.dmz"
	def dockerhubUser = "srvnonproddocker"
	def dockerhubCert = "nonprod-dockerhub"
	//marketplace
	def marketplaceUrl = "http://marketplace-app-03.east1a.dev:3000"
	//openam
	def jenkinsGHECert = "nonprod-openam-cred"
	def openAM = "http://openam-app-10e04e8d.east1c.dev:8080"
	//etc
	def githubUrl = "csp-github.micropaas.io"
	def githubCert = "nonprod-github-cred"
}

class configProd implements Serializable {
	//dockerhub
	def dockerhubUrl = "dhe-app-01.east1a.prod"
	def dockerhubUser = "srvproddocker"
	def dockerhubCert = "prod-dockerhub"
	//marketplace
	def marketplaceUrl = "https://csp-marketplace.sam.gov"
	//openam
	def jenkinsGHECert = "prod-openam-cred"
	def openAM = "http://openam-app-01.micropaas.io:8080"
	//etc
	def githubUrl = "csp-github.sam.gov"
	def githubCert = "prod-github-cred"
}
