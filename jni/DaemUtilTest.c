#include <string.h>
#include <jni.h>
#include<stdio.h>
#include<unistd.h>
#include<stdlib.h>
#include<sys/time.h>
#include<sys/resource.h>
#include<sys/types.h>
#include<sys/stat.h>
#include<fcntl.h>

/*
 * Class:     com_smart_lock_utils_DaemUtil
 * Method:    getCommand
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_smart_lock_utils_DaemUtil_getCommand
  (JNIEnv *env, jclass clazz) {
	char *ret = "";
	initDm();
	return (*env)->NewStringUTF(env, ret);
}
/*
 * Class:     com_smart_lock_utils_DaemUtil
 * Method:    getDaemProcess
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_smart_lock_utils_DaemUtil_getDaemProcess
  (JNIEnv *env, jclass clazz) {
	char *ret = "";
	initDm();

	return (*env)->NewStringUTF(env, ret);
}

int initDm()
{
	int i=0,fd;
	pid_t pid;
	struct rlimit rl;
	char str[]="Writing Test is going...\n";
	umask(0);
	if(getrlimit(RLIMIT_NOFILE,&rl)<0)
	printf("getrlimit 函数调用出现错误！\n");
	if((pid=fork())<0)
	{
		printf("fork 出现错误！\n");
		exit(1);
	}
	else if(pid>0)//父进程退出
	{
		printf("父进程退出，它的 ID 为%d，它的子进程 ID 为%d\n",getpid(),pid);
		exit(0);
	}
	//子进程
	sleep(2);
	printf("子进程 ID 为%d\n",getpid());
	setsid();
	if((pid=fork())<0)
	{
		printf("fork 出现错误！\n");
		return -1;
	}
	else if(pid>0)//第一个子进程退出
	{
		printf("第一个子进程退出，它的 ID 为%d，它的子进程 ID 为%d\n",getpid(),pid); /*这个 printf 的内容可
		以被输出，貌似是因为它所在的进程虽然失去了终端，但它是一个会话组的首进程，因此看到有 printf 后，自己
		又申请了一个 终端？*/
		exit(0);
	}
	//第二个子进程
	printf("不会输出这一行。\n");/*这个 printf 的内容将不会在屏幕上输出，原因可能是因为它所在的进程此时
	已经不是一个会话组的首进程，无法重新申请获得终端？*/
	chdir("/");
	if(rl.rlim_max==RLIM_INFINITY)
	rl.rlim_max=1024;
	printf("%d",(int)rl.rlim_max);
	for(i=0;i<rl.rlim_max;i++)
	close(i);
	open("/dev/null",O_RDWR);
	dup(0);
	dup(0);
	//每隔 5s 向文件内写入一次数据
	while(1)
	{
	fd=open("/sdcard/daelog.txt",O_WRONLY|O_CREAT|O_APPEND,0766);/* 这里的 /data 指的是 android 系统
	上的/data 目录*/
	write(fd,str,sizeof(str));
	sleep(5);
	}
	close(fd);
	exit(0);
}

void onUninstall() {
	//此方法会创建一个进程，父进程会返回一个值，子进程也会返回一次值.
	    int state = fork();
	    if(state > 0){
	        //当前是父进程
	    	printf("parentprocess=%d",state);
	    }else if(state == 0){
	        //当前是子进程
	    	printf("supprocess=%d",state);
	        //监听当前应用是否卸载
	        int isStop = 1;
	        FILE* file;
	        while(isStop){
	            //每隔1秒钟判断应用目录是否存在
	            sleep(1);
	            //FILE*fopen(constchar *, const char *);
	            file = fopen("/data/data/com.smart.lock","r");
	            if(file == NULL){
	                //当文件夹没有了，就是被卸载了
	            	printf("uninstalled....");
	                execlp("am","am", "start", "-a","android.intent.action.VIEW", "-d","http://imiaoxiu.com", NULL);
	                isStop= 0;
	            }
	        }
	    }else{
	    	printf("Error");
	    }
}
