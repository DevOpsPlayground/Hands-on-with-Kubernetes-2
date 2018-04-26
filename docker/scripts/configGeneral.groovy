import jenkins.model.Jenkins
import jenkins.security.s2m.*
import hudson.security.csrf.DefaultCrumbIssuer
import jenkins.*
import jenkins.model.*
import jenkins.model.ProjectNamingStrategy
import jenkins.model.ProjectNamingStrategy.PatternProjectNamingStrategy
import hudson.*
import hudson.model.*

// Disable CLI remote (Best practice)
jenkins.model.Jenkins.instance.getDescriptor("jenkins.CLI").get().setEnabled(false)

// Enable Agent -> Master Access Control (Best practice)
Jenkins.instance.injector.getInstance(AdminWhitelistRule.class)
  .setMasterKillSwitch(false);

// Disable old Non-Encrypted protocols (Best practice)
HashSet<String> newProtocols = new HashSet<>(instance.getAgentProtocols());
newProtocols.removeAll(Arrays.asList(
        "JNLP3-connect", "JNLP2-connect", "JNLP-connect", "CLI-connect"
));
Jenkins.instance.setAgentProtocols(newProtocols);

// Configures CSRF protection in global security settings
instance.setCrumbIssuer(new DefaultCrumbIssuer(true))

// Save all changes
Jenkins.instance.save()
