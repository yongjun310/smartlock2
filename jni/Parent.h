#include <jni.h>
#include <sys/select.h>
#include <unistd.h>
#include <sys/socket.h>
#include <pthread.h>
#include <signal.h>
#include <sys/wait.h>
#include <android/log.h>
#include <sys/types.h>
#include <sys/un.h>
#include <errno.h>
#include <stdlib.h>
#include "ProcessBase.h"
/**
 * 功能：父进程的实现
 */

//子进程有权限访问父进程的私有目录,在此建立跨进程通信的套接字文件
static const char* PATH = "/data/data/com.smart.lock/my.sock";

//服务名称
static const char* SERVICE_NAME =
		"com.smart.lock/.service.LockService";
class Parent: public ProcessBase {
public:

	Parent(JNIEnv* env, jobject jobj);

	virtual bool create_child();

	virtual void do_work();

	virtual void catch_child_dead_signal();

	virtual void on_child_end();

	virtual ~Parent();

	bool create_channel();

	/**
	 * 获取父进程的JNIEnv
	 */
	JNIEnv *get_jni_env() const;

	/**
	 * 获取Java层的对象
	 */
	jobject get_jobj() const;

private:

	JNIEnv *m_env;

	jobject m_jobj;

};
