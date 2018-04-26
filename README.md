# DevOps Playground 20 - Hands on with Kubernetes!
Today we’re going to setup a Jenkins master node within a Kubernetes cluster and configure the Jenkins instance to be able to provision pods on demand; when a pipeline is executed.

These pods will be running a Jenkins JNLP Agent Docker image which will execute our pipeline.

Once the pipeline is completed, the pod will be torn down.

We will be running this demo on AWS instances that have been provision with Docker, kubelet, kubeadm, kubectl and Calico (for container networking);  which you should now have access to.

If you wish to try this on your own machine after the meet-up you can setup a local development environment easily with Minikube.

The preceding Minikube installation instructions are for OSX. The setup for other operating systems is just as easy, but will not be covered here. 

# Install VirtualBox
Download and run through the GUI installer found here:
`https://www.virtualbox.org/wiki/Downloads`

# Install Docker
Follow the below Docker documentation to install Docker for OSX:
`https://docs.docker.com/docker-for-mac/install/`

# Install kubectl
`curl -Lo kubectl https://storage.googleapis.com/kubernetes-release/release/v1.10.0/bin/darwin/amd64/kubectl && chmod +x kubectl && sudo mv kubectl /usr/local/bin/`

Check that it’s installed properly: `kubectl version`

# Install minikube
`curl -Lo minikube https://storage.googleapis.com/minikube/releases/v0.26.1/minikube-darwin-amd64 && chmod +x minikube && sudo mv minikube /usr/local/bin/`

# Getting Started
Let’s create a directory for this playground and cd into it:
`mkdir playground && cd playground`

Let’s create our Jenkins master deployment manifest:
`vi jenkins-deployment.yaml`

*jenkins-deployment.yaml*
```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: jenkins-deployment
  labels:
    app: jenkins
spec:
  replicas: 1
  selector:
    matchLabels:
      app: jenkins
  template:
    metadata:
      labels:
        app: jenkins
    spec:
      containers:
      - name: jenkins
        image: ecsdanthony/pg-jenkins-master:1.0
        ports:
        - containerPort: 8080
        - containerPort: 50000
        volumeMounts:
          - name: jenkins-home
            mountPath: /var/jenkins_home
      volumes:
        - name: jenkins-home
          emptyDir: {}
```

Deploy this to the cluster:
`kubectl apply -f jenkins-deployment.yaml`

Once deployed, this creates a pod wherein Jenkins is running in a container.

Find the name of the pod just created:
`kubectl get pods`
Check the status of the pod:
`kubectl describe pod {POD-NAME}`

# Create a Jenkins Service
Let’s create our Jenkins service deployment manifest:
`vi jenkins-service.yaml`

*jenkins-service.yaml*
```
apiVersion: v1
kind: Service
metadata:
  name: jenkins
spec:
  type: NodePort
  externalTrafficPolicy: Local
  ports:
    - port: 8080
      name: "http"
      targetPort: 8080
      protocol: TCP
    - port: 50000
      name: "agent"
      protocol: TCP
  selector:
    app: jenkins
```

Create the service:
`kubectl create -f jenkins-service.yaml`

# Accessing the Kubernetes Dashboard
The dashboard for your cloud instance can be found at:
`http://{animal}.devopsplayground.com:8001/api/v1/namespaces/kube-system/services/http:kubernetes-dashboard:/proxy/#!/overview?namespace=default`

# Accessing the Jenkins UI
Find the port Jenkins is running on, mapped to 8080:
`kubectl get services`

Access the UI via:
`http://{animal}.devopsplayground.com:{port}`

# Configuring Jenkins
Within the Jenkins UI, navigate to “*Manage Jenkins* -> *Configure System*”

Set: `# of executors` to `0`

Scroll down to the bottom of the form, under “*Cloud*”, select “*Add new Cloud* -> *Kubernetes*” and populate it with the following information:

**Kubernetes URL**: `https://{animal}.devopsplayground.com:6443`
**Disable https certificate check**: `Enabled`
**Kubernetes Namespace**: `default`

Now use the “*Test Connection*” button and if all goes according to plan you should see a “*Connection test successful*” message below.

Assuming that’s correct, continuing by entering the following details on the same form:

**Jenkins URL**: `http://{animal}.devopsplayground.com:{port}`
Where the port number is the randomly mapped http port, which you received earlier by running `kubectl get service`.
**Jenkins tunnel**: `{animal}.devopsplayground.com:{port}`
Where the port number is the randomly mapped agent port, which you received earlier by running `kubectl get service`.

Now let’s set up a pod template by clicking “*images* -> *Add Pod Template* -> *Kubernetes Pod Template*” and populate the form with the following information:

**Name**: `jnlp-slave`
**Namespace**: `default`

Set up the container template by clicking “*Add Container*” and populating it like such:

**Name**: `jnlp`
**Docker Image**: `jenkins/jnlp-slave`
**Always pull image**: `Enabled`
**Arguments to pass to the command**: `${computer.jnlpmac} ${computer.name}`

Jenkins should now be set up to provision pods!

# A Basic Jenkins Pipeline
Let’s now create a basic pipeline to test the functionality we just configured.

From the Jenkins homepage, create a pipeline by navigating to “*New Item* -> *Pipeline*” and name is accordingly.

Scroll down to the bottom of the form and in the Pipeline definition script field, use the following script:

```
def label = "mypod-${UUID.randomUUID().toString()}"
podTemplate(label: label) {
    node(label) {
        stage('Run shell') {
            sh 'echo hello world'
        }
    }
}
```

Save your pipeline.

# Running the Pipeline
On the left hand side of the page, you should see a “*Build Now*” link; select this to kick off a build to test your pipeline.

Jenkins will now provision a pod using the *Jenkins/jnlp-slave* image we set earlier. This pod will then execute the pipeline; in our case, echoing hello world.

It should take ~30 seconds to provision and run the pipeline.

You can check the status of the newly created pod as it’s being provisioned by running the command `kubectl get pods` and looking for a pod named `jenkins-slave-*`.

Alternatively you can see this in the Kubernetes Dashboard.

If all goes according to plan, you should see that the pipeline has completed successfully!

That concludes this DevOps Playground Meet-Up.

If you have any queries feel free to drop me an email @ `anthony.forster@ecs-digital.co.uk`

I hope you’ve had a good time and that this has been a good glimpse into the possibilities of running Jenkins on Kubernetes.

Please help yourself to beer and pizza! :)