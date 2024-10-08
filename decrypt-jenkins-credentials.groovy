// Here is a short snippet you can just run from the jenkins script console, to dump all of your credentials to plain text.

com.cloudbees.plugins.credentials.SystemCredentialsProvider.getInstance().getCredentials().forEach{
  it.properties.each { prop, val ->
    println(prop + ' = "' + val + '"')
  }
  println("-----------------------")
}


// A more complicated version that lists for non-system credential providers:

import com.cloudbees.plugins.credentials.CredentialsProvider
import com.cloudbees.plugins.credentials.Credentials
import com.cloudbees.plugins.credentials.domains.Domain
import jenkins.model.Jenkins
def indent = { String text, int indentationCount ->
  def replacement = "\t" * indentationCount
  text.replaceAll("(?m)^", replacement)
}

Jenkins.get().allItems().collectMany{ CredentialsProvider.lookupStores(it).toList()}.unique().forEach { store ->
  Map<Domain, List<Credentials>> domainCreds = [:]
  store.domains.each { domainCreds.put(it, store.getCredentials(it))}
  if (domainCreds.collectMany{ it.value}.empty) {
    return
  }
  def shortenedClassName = store.getClass().name.substring(store.getClass().name.lastIndexOf(".") + 1)
  println "Credentials for store context: ${store.contextDisplayName}, of type $shortenedClassName"
  domainCreds.forEach { domain , creds ->
    println indent("Domain: ${domain.name}", 1)
    creds.each { cred ->
      cred.properties.each { prop, val ->
        println indent("$prop = \"$val\"", 2)
      }
      println indent("-----------------------", 2)
    }
  }
}


// For the record, The following snippet to be pasted into the console also does the job :

def creds = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(
    com.cloudbees.plugins.credentials.common.StandardUsernameCredentials.class,
    Jenkins.instance,
    null,
    null
)

for(c in creds) {
  if(c instanceof com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey){
    println(String.format("id=%s  desc=%s key=%s\n", c.id, c.description, c.privateKeySource.getPrivateKeys()))
  }
  if (c instanceof com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl){
    println(String.format("id=%s  desc=%s user=%s pass=%s\n", c.id, c.description, c.username, c.password))
  }
}


// Source: https://devops.stackexchange.com/questions/2191/how-to-decrypt-jenkins-passwords-from-credentials-xml
