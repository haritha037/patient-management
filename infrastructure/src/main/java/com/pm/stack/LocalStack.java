package com.pm.stack;


import software.amazon.awscdk.*;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.Protocol;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.msk.CfnCluster;
import software.amazon.awscdk.services.rds.*;
import software.amazon.awscdk.services.route53.CfnHealthCheck;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LocalStack extends Stack {
    private final Vpc vpc; // class level due to connection requirements
    private final Cluster ecsCluster;


    public LocalStack(final App scope, final String id, final StackProps props){
        // there are some things that should be passed to the parent class
        // Use the constructor to do it
        super(scope, id, props);
        this.vpc = createVpc();

        DatabaseInstance authServiceDb = createDatabase("AuthServiceDB", "auth-service-db");
        DatabaseInstance patientServiceDb = createDatabase("PatientServiceDB", "patient-service-db");

        CfnHealthCheck authDBHealthCheck = createDbHealthCheck(authServiceDb, "AuthServiceDBHealthCheck");
        CfnHealthCheck patientDBHealthCheck = createDbHealthCheck(patientServiceDb, "PatientServiceDBHealthCheck");

        CfnCluster mskCluster = createMskCluster();

        this.ecsCluster = createEcsCluster();

    }



    private Vpc createVpc(){
        return Vpc.Builder
                .create(this, "PatientManagementVPC")
                .vpcName("PatientManagementVPC")
                .maxAzs(2) // max availability zones(where do we want our infrastructure to be distributed)
                .build();
    }

    private DatabaseInstance createDatabase(String id, String dbName){
        return DatabaseInstance.Builder
                .create(this, id)
                .engine(
                        DatabaseInstanceEngine.postgres(
                                PostgresInstanceEngineProps.builder()
                                        .version(PostgresEngineVersion.VER_17_2)
                                        .build()
                        )
                )
                .vpc(vpc) // this will connect the db to the vpc
                .instanceType(InstanceType.of(
                        InstanceClass.BURSTABLE2,
                        InstanceSize.MICRO)) // combination of CPU, computer power, storage (specs)
                .allocatedStorage(20)
                .credentials(Credentials.fromGeneratedSecret("admin_user"))
                .databaseName(dbName)
                .removalPolicy(RemovalPolicy.DESTROY) // everytime we destroy the stack, destroy the database storage
                .build();
    }

    private CfnHealthCheck createDbHealthCheck(DatabaseInstance db, String id){
        return CfnHealthCheck.Builder
                .create(this, id)
                .healthCheckConfig(CfnHealthCheck.HealthCheckConfigProperty.builder()
                        .type("TCP")
                        .port(Token.asNumber(db.getDbInstanceEndpointPort()))
                        .ipAddress(db.getDbInstanceEndpointAddress())
                        .requestInterval(30) // check the status every 30 seconds
                        .failureThreshold(3) // try 3 times before reporting a failure
                        .build())
                .build();
    }

    private CfnCluster createMskCluster(){
        return CfnCluster.Builder
                .create(this, "MskCluster")
                .clusterName("kafka-cluster")
                .kafkaVersion("2.8.0")
                .numberOfBrokerNodes(1)
                .brokerNodeGroupInfo(CfnCluster.BrokerNodeGroupInfoProperty.builder()
                        .instanceType("kafka.m5.xlarge")
                        // connect the vpc to all broker nodes
                        .clientSubnets(
                                vpc.getPrivateSubnets().stream()
                                .map(ISubnet::getSubnetId)
                                .collect(Collectors.toList())
                        )
                        .brokerAzDistribution("DEFAULT") // how brokers are distributed across availability zones
                        .build()
                )
                .build();

    }

    private Cluster createEcsCluster() {
        return Cluster.Builder
                .create(this, "PatientManagementCluster")
                .vpc(vpc)
                // set up a cloud map namespace called "patient-management.local" for service discovery in ECS
                // This allows microservices to find and communicate with each other using this domain
                // Ex: auth-service.patient-management.local  - convenient(we do not need to know internal ip addresses)
                // server-name + name-space
                // like internal network in docker(all the containers find each other using the container name)
                .defaultCloudMapNamespace(CloudMapNamespaceOptions.builder()
                        .name("patient-management.local")
                        .build())
                .build();
    }

    private FargateService createFargateService(
            String id,
            String imageName,
            List<Integer> ports,
            DatabaseInstance db,
            Map<String, String> additionalEnvVars
    ){
        // ECS TASK runs the container
        // To create an ECS TASK, we need a TASK DEFINITION(blueprint)

        FargateTaskDefinition taskDefinition = FargateTaskDefinition.Builder
                .create(this, id+"Task")
                .cpu(256)
                .memoryLimitMiB(512)
                .build();

        // specify the actual container that we want this task to run
        ContainerDefinitionOptions containerOptions = ContainerDefinitionOptions.builder()
                // task will pull an image from a repository
                // we can specify where an image comes from (Ex: ECR-AWS's container registry)
                // LocalStack know to pull the image from the local image repository
                .image(ContainerImage.fromRegistry(imageName))

                .portMappings(ports.stream()
                        .map(port -> PortMapping.builder()
                                .containerPort(port) // the port on which the application runs inside the container
                                .hostPort(port) // exposed port, so that other services can access it
                                .protocol(Protocol.TCP)
                                .build())
                        .toList())

                // create a log group for each container
                .logging(LogDriver.awsLogs(AwsLogDriverProps.builder()
                                .logGroup(LogGroup.Builder
                                        .create(this, id+"LogGroup")
                                        .logGroupName("/ecs/" + imageName)
                                        .build()
                                )
                        .build()))
                .build();
    }

    public static void main(final String[] args) {
        // create an AWS CDK app and define where the output to be
        // whenever the stack is created, a cloudformation template is created
        // this template will go to cdk.out folder
        App app = new App(AppProps.builder().outdir("./cdk.out").build());

        // define StackProps
        // additional properties that we want to apply to the stack
        // synthesizer - used to convert our code into a cloudformation template
        // Bootstrapless - skip the initial bootstrapping of the cdk env as we do not need it for localstack
        StackProps props = StackProps.builder()
                .synthesizer(new BootstraplessSynthesizer())
                .build();

        // link the LocalStack class to this app
        // so that cdk knows to build our stack when we run this java app
        new LocalStack(app, "localstack", props);

        // tell the cdk app to
        // take our stack, add the props, convert that stack to a cloudformation template
        // and put everything into cdk.out folder
        app.synth();

        System.out.println("App synthesizing in progress.......");
    }

}
