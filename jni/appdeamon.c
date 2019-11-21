#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <getopt.h>
#include <string.h>	
//#include <asprintf.h>
#include <unistd.h>

int chk_process(const char *process_name)
{
  FILE   *stream;
  char   *line = NULL;
  size_t len = 0;
  ssize_t read_len;

  stream = popen( "ps", "r" );
  if (stream == NULL)
    return -1;

  int exists = 0;
  while ( (read_len = getline(&line, &len,  stream)) != -1)
  {
      int len = strlen(line);
    char *cmd = line + len;
    while ( len >0 ) 
    {    
        len--;
        if ( *cmd == ' ')
        {
            cmd++;
            break;
        }

        cmd--;
      }

    if( strncmp(cmd, process_name, strlen(process_name)) == 0 )
    {
      exists = 1;
      break;
    }
  }

  pclose( stream );
  if ( line != NULL )
    free(line);

  return exists;
}

void run_service(const char *process_name, const char *package_name, const char *activity_name, int interval_sec)
{
  while (1)
  {
    if ( chk_process(process_name) == 0)
    {
      char *pkg_activity_name = NULL;
      // 格式化命令
      asprintf(&pkg_activity_name, "/system/bin/am start --user 0 -n %s/%s", package_name, activity_name);
      system(pkg_activity_name);// 执行命令启动app
      free(pkg_activity_name);
    }
     // sleep 指定时间间隔
    sleep(interval_sec);
  }

  return;
}

int main( int argc, char* argv[]  )  
{  
  signal(SIGTERM, SIG_IGN);
  printf("enter main");
  const char *process_name = NULL;
  const char *package_name = NULL;
  const char *activity_name = NULL;
  int interval_sec = 30;

  struct option options[] =
    {
    { "process_name", required_argument, 0, 'p' },
    { "package_name", required_argument, 0, 'a' },
    { "activity_name", required_argument, 0, 'c' },
    { "interval_sec", required_argument, 0, 'i' },
    { 0, 0, 0, 0 }
    };

    int c;

    for (;;)
    {
        c = getopt_long(argc, argv, "p:a:c:i:", options, NULL);
        if (c == -1)
        {
            break;
        }
        switch (c)
        {
        case 'p':
            process_name = optarg;
            break;
        case 'a':
            package_name = optarg;
            break;
        case 'c':
            activity_name = optarg;
            break;
        case 'i':
            interval_sec = atoi(optarg);
            break;
        default:
            exit(EXIT_FAILURE);
        }
    }

    if (process_name == NULL || package_name == NULL || activity_name == NULL)
        exit(EXIT_FAILURE);

  daemon(1, 1);

  run_service(process_name, package_name, activity_name, 10);

  return 0;
}