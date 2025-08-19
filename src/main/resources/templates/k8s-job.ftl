apiVersion: batch/v1
kind: Job
metadata:
  name: ${jobName}
  labels:
    app.kubernetes.io/instance: job-orchestrator-instance
    app.kubernetes.io/name: job-orchestrator  
spec:
  ttlSecondsAfterFinished: 120
  template:
    spec:
      containers:
        - name: job-runner
          image: ${imageName}
          <#if envVariables?? && (envVariables?size > 0)>
          env:
          <#list envVariables?keys as key>
            - name: ${key?upper_case}
              value: "${envVariables[key]}"
          </#list>
          </#if>
          <#if command?? && (command?size > 0)>
          command:
          <#list command as cmd>
            - "${cmd}"
          </#list>
          </#if>
          <#if pvcName??>
          volumeMounts:
            - name: data-volume
              mountPath: "/mnt/data"
          </#if>
      <#if pvcName??>
      volumes:
        - name: data-volume
          persistentVolumeClaim:
            claimName: ${pvcName}
      </#if>
      restartPolicy: Never
