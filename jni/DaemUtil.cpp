#include "Parent.h"
#include <stdio.h>
#include <string.h>
#include <jni.h>
#include<unistd.h>
#include<stdlib.h>
#include<sys/time.h>
#include<sys/resource.h>
#include<sys/types.h>
#include<sys/stat.h>
#include<fcntl.h>


/**
* 全局变量，代表应用程序进程.
*/
Parent *g_process = NULL;

/**
* 应用进程的UID.
*/
const char* g_userId = NULL;

/**
* 全局的JNIEnv，子进程有时会用到它.
*/
JNIEnv* g_env = NULL;


int initDm()
{
	int i=0,fd;
	pid_t pid;
	struct rlimit rl;
	char str[]="Writing Test is going...\n";
	umask(0);

	if(getrlimit(RLIMIT_NOFILE,&rl)<0)
		LOGE("getrlimit 函数调用出现错误！\n");
	if((pid=fork())<0)
	{
		LOGE("fork 出现错误！\n");
		exit(1);
	}
	else if(pid>0)//父进程退出
	{
		//LOGE("父进程退出，它的 ID 为%d，它的子进程 ID 为%d\n",getpid(),pid);
	}
	//子进程
	sleep(15);
	//LOGE("sub process ID is %d\n",getpid());
	setsid();
	if((pid=fork())<0)
	{
		printf("fork 出现错误！\n");
		return -1;
	}
	else if(pid>0)//第一个子进程退出
	{
		//LOGE("first process ID is %d，sub process ID is%d\n",getpid(),pid);
		exit(0);
	}
	setsid();
	//第二个子进程
	chdir("/");
	if(rl.rlim_max==RLIM_INFINITY)
		rl.rlim_max=1024;
	//LOGE("%d",(int)rl.rlim_max);
	for(i=0;i<rl.rlim_max;i++)
		close(i);
	open("/dev/null",O_RDWR);
	dup(0);
	dup(0);
		//LOGE("send an am command");
	execlp("am", "am", "startservice", "--user", "0",
					"-n", "com.smart.lock/com.smart.lock.service.HeartBeatService", (char*) NULL);
	close(fd);
	exit(0);
}

/*
 * Class:     com_smart_lock_utils_DaemUtil
 * Method:    connectToMonitor
 * Signature: ()Z
 */

char* jstringTostring(JNIEnv* env, jstring jstr)  
{  
    char* rtn = NULL;  
    jclass clsstring = env->FindClass("java/lang/String");  
    jstring strencode = env->NewStringUTF("utf-8");  
    jmethodID mid = env->GetMethodID(clsstring, "getBytes", "(Ljava/lang/String;)[B");  
    jbyteArray barr = (jbyteArray)env->CallObjectMethod(jstr, mid, strencode);  
    jsize alen = env->GetArrayLength(barr);  
    jbyte* ba = env->GetByteArrayElements(barr, JNI_FALSE);  
    if (alen > 0)  
    {  
        rtn = (char*)malloc(alen + 1);  
        memcpy(rtn, ba, alen);  
        rtn[alen] = 0;  
    }  
    env->ReleaseByteArrayElements(barr, ba, 0);  
    return rtn;  
}  

extern "C" JNIEXPORT jboolean JNICALL Java_com_smart_lock_utils_DaemUtil_createWatcher( JNIEnv* env, jobject thiz, jstring user )
{
	/*g_process = new Parent( env, thiz );
	
	g_userId  = (const char*)jstringTostring(env, user);
	
	
	g_process->catch_child_dead_signal();
	
	if( !g_process->create_child() )
	{
		printf("<<create child error!>>");
		
		return JNI_FALSE;
	}*/
	//LOGE("ENTER createWatcher\n");
	initDm();
	return JNI_TRUE;
}


extern "C" JNIEXPORT jboolean JNICALL Java_com_smart_lock_utils_DaemUtil_connectToMonitor(JNIEnv* env, jobject thiz )
{
	if( g_process != NULL )
	{
		if( g_process->create_channel() )
		{
			return JNI_TRUE;
		}
		
		return JNI_FALSE;
	}
}
