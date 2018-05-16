#!/usr/bin/python3

#数据库连接模块 
import pymysql
import datetime

#共享单车系统 网络交互 功能模块
from socket import *
from time import ctime

db=""
cursor=""
def db_connect():
    global db
    global cursor
    
    # 打开数据库连接
    db = pymysql.connect(host="67.209.186.100",user="root",passwd="XXXXXXXXX",db="mysql")
 
    # 使用 cursor() 方法创建一个游标对象 cursor
    #使用cursor()方法获取操作游标
    cursor = db.cursor()
 
    # 使用 execute()  方法执行 SQL 查询 
    cursor.execute("SELECT VERSION()")
 
    # 使用 fetchone() 方法获取单条数据.
    data = cursor.fetchone()
 
    print ("Database version : %s " % data)

    db = pymysql.connect(host="67.209.186.100",user="root",passwd="XXXXXXXXXX",db="db_sharedbike")
    cursor = db.cursor()
    

def db_dis_connect(): 
    global db
    # 关闭数据库连接
    try:
        db.close()
    except Exception as e:
        print("断开数据库失败，请先连接数据库！！！")
        print(e)


def newtwork_activate():
    HOST = ""
    PORT = 10000
    BUFSIZ = 1024
    ADDR = (HOST, PORT)


    tcpSerSock = socket(AF_INET, SOCK_STREAM)
    tcpSerSock.bind(ADDR)
    tcpSerSock.listen(10)

 
    while True:
        print("服务器启动， waiting for connection...")
        db_connect()
        print("数据库已连接！！！")
        tcpCliSock, addr = tcpSerSock.accept()
        print("本次网络连接已经成功建立！！！")
        print("connected from :", addr)

        try:
            #防止网络攻击，扫描等发送非法请求
            data = tcpCliSock.recv(BUFSIZ).decode("utf-8")
            recv_message_list=data.split()
        except Exception as e:
            print(e)
            continue
            

        print(recv_message_list)


        if(recv_message_list==[]):
            print("网络中断")
        elif(recv_message_list[0]=="login_validate"):
            "登入服务器 用户名及密码的验证"
            print(recv_message_list)
            login_validate(tcpCliSock, recv_message_list)
        
        elif(recv_message_list[0]=="bike_num"):
            "上传车的编号  需要开锁的自行车编号"
            open_bike_num(tcpCliSock, recv_message_list)
        
        elif(recv_message_list[0]=="bike_position"):
            "上传车的位置 获取附近车的坐标"
            bike_position(tcpCliSock, recv_message_list)

    
        elif(recv_message_list[0]=="user_name"):
            "上传用户名  用来下载用户详细信息"
            user_info(tcpCliSock, recv_message_list)


        elif(recv_message_list[0]=="money_add"):
            "上传用户名 充值金额    进行充值操作"
            money_add(tcpCliSock, recv_message_list)

        elif(recv_message_list[0]=="user_info_changed"):
            "上传用户名 电话号码  邮箱地址"
            info_changed(tcpCliSock, recv_message_list)

        elif(recv_message_list[0]=="message_push_request"):
            "推送消息请求"
            message_push_request(tcpCliSock, recv_message_list)

        elif(recv_message_list[0]=="history_push_request"):
            "推送历史订单请求"
            history_push_request(tcpCliSock, recv_message_list)

        elif(recv_message_list[0]=="order_finished"):
            "推送历史订单请求"
            order_finished(tcpCliSock, recv_message_list)
        elif(recv_message_list[0]=="enroll_validate"):
            "用户注册请求"
            user_enroll(tcpCliSock, recv_message_list)

        db_dis_connect()
        print("数据库已经断开连接！！！")

    tcpSerSock.close()    

    

def login_validate(tcpCliSock, recv_message_list):
    #接收信息未进行处理
    #"login_validate"+" "+c_string_0+" "+c_string_1  （用户名，密码）
    #需要做的工作是验证  用户名，密码是否存在并正确
    
    #message="1"+'\r\n'   #1为正确，成功登入，  0为错误，拒绝登入
    #message="0"+'\r\n'
    #content = '[%s] %s' % (bytes(ctime(), "utf-8"), data)
    #tcpCliSock.send(content.encode("utf-8"))
    #print(message)

    message=""

    def select_name_pass():
        nonlocal message
        sql="select password from user_info where username='%s'"%(recv_message_list[1])

        try:
            cursor.execute(sql)
            result=cursor.fetchone()

            print(result)
            
            if result==None:
                #没有改用户名
                print(0)
                message="0"+'\r\n'
            elif result[0]==recv_message_list[2]:
                #密码正确
                print(1)
                message="1"+'\r\n'
            else:
                #密码不正确
                print(2)
                message="0"+'\r\n'
            
        except Exception as e:
            #数据库连接发生错误
            message="0"+'\r\n'
            print ("Error: unable to fetch data")
            print (e)

            
    select_name_pass()
    tcpCliSock.sendall(message.encode('utf-8'))
    tcpCliSock.close()
    print("该次网络连接已经断开！！！")


def open_bike_num(tcpCliSock, recv_message_list):
    #"bike_num"+" "+user_name+" "+stateTitile（车编号）
    #需要做的工作是  为用户 user_name   将 stateTitle编号的自行车开启，若是可以开启返回1，否则0
    #如果有欠费  或者  有订单没有完成  也返回 0  ， 开锁失败
    
    #接收信息未进行处理
    #message="1"+'\r\n'     #1为正确，成功开锁，  0为错误，拒绝开锁
    #message="0"+'\r\n'

    message=""
    user_id=0
    state_0=0
    state_1=0
    state_2=0

    #判断用户是否有欠费
    def user_money_enough():
        nonlocal state_0
        nonlocal user_id
        sql="select money,id from user_info where username='%s'"%(recv_message_list[1])

        try:
            cursor.execute(sql)
            result=cursor.fetchone()

            print(result)
            
            if result==None:
                #没有该用户名,该情况应该不存在
                print(0)
                state_0=0
                user_id=0
            if result[0]<=0:
                #用户已经欠费
                print(1)
                state_0=0
                user_id=result[1]
            else:
                #用户有余额
                print(2)
                state_0=1
                user_id=result[1]
                
        except Exception as e:
            #数据库连接发生错误
            state_0=0
            print ("Error: unable to fetch data")
            print (e)


        
    #判断用户是否有订单未完成
    def user_unfinished():
        nonlocal state_1
        nonlocal user_id
        sql="select return_time from order_info where user_id='%d'"%(user_id)

        try:
            cursor.execute(sql)
            result=cursor.fetchall()

            print(result)

            if result==None:
                #没有信息,出错
                print(0)
                state_1=0
            
            if result==():
                #没有信息,该用户没有下过订单
                print(0)
                state_1=1
            elif result[-1][0]==datetime.datetime(1990,1,1,1,1):
                #用户存在没有还车的情况
                print(1)
                state_1=0
            else:
                #用户订单都已完成
                print(2)
                state_1=1
                
        except Exception as e:
            #数据库连接发生错误
            state_1=0
            print ("Error: unable to fetch data")
            print (e)

    
    #判断自行车是否存在，存在是否可以开启
    def select_bike_state():
        nonlocal state_2
        sql="select state from bike_info where id='%d'"%(int(recv_message_list[2]))

        try:
            cursor.execute(sql)
            result=cursor.fetchone()

            print(result)
            
            if result==None:
                #没有该车
                print(0)
                state_2=0
            if result[0]==1:
                #可以借车
                print(1)
                state_2=1
            else:
                #不可以借车
                print(2)
                state_2=0
            
        except Exception as e:
            #数据库连接发生错误
            state_2=0
            print ("Error: unable to fetch data")
            print (e)

    insert_state=0
    insert_state2=0
    def insert_order():
        #user_id  int(recv_message_list[2])
        #nonlocal user_id
        nonlocal insert_state
        nonlocal insert_state2
        t=datetime.datetime.now().strftime('%Y-%m-%d %H:%M')
        sql="insert into order_info(user_id, bike_id, take_time) values ('%d', '%d', '%s')"%(user_id, int(recv_message_list[2]), str(t))
        sql2="update bike_info set state=0 where id='%d'"%(int(recv_message_list[2]))

        """
        try:
            cursor.execute(sql)
            db.commit()
            insert_state=1
        except Exception as e:
            #数据库连接发生错误
            insert_state=0
            db.rollback()
            print ("Error: unable to fetch data")
            print (e)

        try:
            cursor.execute(sql2)
            db.commit()
            insert_state2=1
        except Exception as e:
            #数据库连接发生错误
            insert_state2=0
            db.rollback()
            print ("Error: unable to fetch data")
            print (e)
        """
        try:
            cursor.execute(sql)
            cursor.execute(sql2)
            db.commit()
            insert_state=1
            insert_state2=1
            print("数据更新！！！")
        except Exception as e:
            #数据库连接发生错误
            insert_state=0
            insert_state2=0
            db.rollback()
            print ("Error: unable to fetch data")
            print (e)

    user_money_enough()
    user_unfinished()
    select_bike_state()

    if (state_0+state_1+state_2)==3:
        #可以借车
        insert_order()
        print(insert_state+insert_state2)
        if (insert_state+insert_state2)==2:
            message="1"+'\r\n'
            print(111)
        else:
            message="0"+'\r\n'
            print(222)
    else:
        message="0"+'\r\n'
        print(333)
    
    tcpCliSock.sendall(message.encode('utf-8'))
    tcpCliSock.close()


def bike_position(tcpCliSock, recv_message_list):
    #  "bike_position"+" "+amapLocation_public.getLatitude()+" "+amapLocation_public.getLongitude()
    #需要做的工作是 根据收到的 经度 纬度 坐标， 给出附近的自行车 编号，坐标
    
    #接收信息未进行处理      
    """
    results=["1 39.083746 121.813489"+'\r\n',
                  "2 39.083746 121.813589"+'\r\n',
                  "3 39.083646 121.813689"+'\r\n',
                  "4 39.083346 121.813189"+'\r\n',
                  "5 39.083946 121.813089"+'\r\n',
                  "end\r\n"]
    """
    message=""
    def select_bike_position():
        nonlocal message
        sql="select id,lat,lon from bike_info where state='%d'"%(1)

        try:
            cursor.execute(sql)
            results=cursor.fetchall()

            print(results)
            
            if results==None:
                #没有可用的自行车
                print(0)
                message="end"+'\r\n'
            else:
                message=''.join(str(each_tuple[0])+" "+str(each_tuple[1])+" "+str(each_tuple[2])+"\r\n" for each_tuple in results)
        except Exception as e:
            #数据库连接发生错误
            message="end"+'\r\n'
            print ("Error: unable to fetch data")
            print (e)
        
    select_bike_position()
    
    tcpCliSock.sendall(message.encode('utf-8'))
    tcpCliSock.close()
    

def user_info(tcpCliSock, recv_message_list):
    #  "user_name"+" "+user_name
    #需要做的是返回  user_name  用户的详细信息

    state=0
    message=""
    message_list=[]
    #接收信息未进行处理
    def select_user_info():
        nonlocal state
        nonlocal message
        nonlocal message_list
        sql="select phonenum, email, money, minutes from user_info where username='%s'"%(recv_message_list[1])

        try:
            cursor.execute(sql)
            result=cursor.fetchone()

            print(result)
            
            if result==None:
                #没有该用户
                print(0)
                state=0
            else:
                state=1
                message_list=[result[0],
                  result[1],
                  str(result[2]),
                  str(result[3]//60),
                  str(result[3]%60)]
                print(message_list)

        except Exception as e:
            #数据库连接发生错误
            state=0
            print ("Error: unable to fetch data")
            print (e)
            message_list=["15998253976"+' ',
                  "812839666@qq.com"+' ',
                  "106.00"+' ',
                  "16"+' ',
                  "16"]

    select_user_info()        
    message=" ".join(message_list)
    message+='\r\n'
    
    tcpCliSock.sendall(message.encode('utf-8'))
    tcpCliSock.close()
    

def money_add(tcpCliSock, recv_message_list):
    #  "money_add"+" "+user_name+" "+money_input_string
    #  将金额 money_input_string  加入到  user_name 名下
    #  不需要返回信息
    def money_insert():

        sql="update user_info set money='%f'+money where username='%s'"%(float(recv_message_list[2]), recv_message_list[1])

        try:
            cursor.execute(sql)
            db.commit()
            print("充值成功")

        except Exception as e:
            #数据库连接发生错误
            print ("Error: unable to fetch data")
            print (e)
            db.rollback()
            
    money_insert()
        
    tcpCliSock.close()


def info_changed(tcpCliSock, recv_message_list):
    #  "user_info_changed"+" "+user_name+" "+phone_string+" "+email_string    返回修改后的用户信息，需写入数据库
    #  不需要返回信息
    def inner_info_changed():
        sql="update user_info set phonenum='%s',email='%s' where username='%s'"%(int(recv_message_list[2]), recv_message_list[3], recv_message_list[1])

        try:
            cursor.execute(sql)
            db.commit()
            print("用户信息修改成功")
        except Exception as e:
            #数据库连接发生错误
            print ("Error: unable to fetch data")
            print (e)
            db.rollback()

    inner_info_changed()
    
    tcpCliSock.close()


def message_push_request(tcpCliSock, recv_message_list):
    #  "message_push_request"+" "+num                根据请求的ID，推送消息
    #  0则推送 （0-4）     1则推送 （5-9）   2则推送 （10-14）    num*5 -((num+1)*5-1)
    #
    #   返回码 为 0,1,2,3,4,5   0表示失败，没有消息，   1表示推送为1条  2表示推送为2条
    message_list=["2\r\n",
                  "hello world"+'\r\n',
                  "2018.01.01"+' ',
                  "http://www.baidu.com"+'\r\n',
                  "hello world333"+'\r\n',
                  "2018.01.01"+' ',
                  "http://www.hao123.com"+'\r\n',
                  ]
    num=int(recv_message_list[1])
    def select_message_info():
        nonlocal message_list
        
        sql="select id, title, pub_date, pub_url from message_info"

        try:
            cursor.execute(sql)
            result=cursor.fetchall()

            print(result)
            
            if result==None:
                #没有该用户
                print(0)
                state=0
            else:
                result=result[::-1]
                result=result[num*5:((num+1)*5)]
                message_list=[str(len(result))+"\r\n"]
                for each in result:
                    message_list.append(each[1].encode('latin1').decode('utf-8')+"\r\n")
                    message_list.append(str(each[2].strftime('%Y-%m-%d')+" "))
                    message_list.append(each[3]+"\r\n")
                print(message_list)
        except Exception as e:
            #数据库连接发生错误
            print ("Error: unable to fetch data")
            print (e)

    select_message_info()
    message="".join(message_list)
    tcpCliSock.sendall(message.encode('utf-8'))
    
    tcpCliSock.close()
    

def history_push_request(tcpCliSock, recv_message_list):
    #  "history_push_request"+" "+num+" "+c_string        根据请求的ID，推送消息,  c_string用户名
    #  0则推送 （0-4）     1则推送 （5-9）   2则推送 （10-14）    num*5 -((num+1)*5-1)
    #
    #   返回码 为 0,1,2,3,4,5   0表示失败，没有消息，   1表示推送为1条  2表示推送为2条
    message_list=["3\r\n",
                  "0#"+'\r\n',
                  "2018年4月20日 09时30分"+'\r\n',
                  "1990年1月1日 01时01分"+'\r\n',
                  "1#"+'\r\n',
                  "2018年4月20日 09时30分"+'\r\n',
                  "2018年4月20日 09时31分"+'\r\n',
                  "2#"+'\r\n',
                  "2018年4月20日 09时35分"+'\r\n',
                  "2018年4月20日 09时36分"+'\r\n',
                  ]

    def select_history_info():
        nonlocal message_list
        num=int(recv_message_list[1])
        user_id=0
        
        sql="select id from user_info where username='%s'"%(recv_message_list[2])

        try:
            cursor.execute(sql)
            result=cursor.fetchone()

            print(result)
            
            if result==None:
                #没有该用户
                print(0)
            else:
                print(1)
                user_id=result[0]

        except Exception as e:
            #数据库连接发生错误
            print ("Error: unable to fetch data")
            print (e)


        sql2="select id, take_time, return_time from order_info where user_id='%d'"%(user_id)

        try:
            cursor.execute(sql2)
            result=cursor.fetchall()

            print(result)
            
            if result==None:
                #没有该用户
                print(0)
            else:
                result=result[::-1]
                result=result[num*5:((num+1)*5)]
                message_list=[str(len(result))+"\r\n"]
                for each in result:
                    message_list.append(str(each[0])+"\r\n")
                    message_list.append(str(each[1].strftime('%Y-%m-%d  %H:%M')+"\r\n"))
                    message_list.append(str(each[2].strftime('%Y-%m-%d  %H:%M')+"\r\n"))
                print(message_list)
        except Exception as e:
            #数据库连接发生错误
            print ("Error: unable to fetch data")
            print (e)

    select_history_info()
    
    message="".join(message_list)
    tcpCliSock.sendall(message.encode('utf-8'))
    
    tcpCliSock.close()


def order_finished(tcpCliSock, recv_message_list):
    #  "order_finished"+" "+message[0]        order_finished 为请求标志    订单号
    #

    message="1"+'\r\n'     #1为正确，成功结账，  0为错误，结账失败    ***正常不存在结账失败问题
    #都是返回 1 就可以
    order_id=int(recv_message_list[1])

    def all_finished():
        user_id=0
        bike_id=0
        
        #order_info 还车时间
        t=datetime.datetime.now().strftime('%Y-%m-%d %H:%M')
        sql="update order_info set return_time='%s' where id='%d'"%(str(t), order_id)
        try:
            cursor.execute(sql)
            db.commit()
        except Exception as e:
            db.rollback()
            #数据库连接发生错误
            print ("Error: unable to fetch data")
            print (e)

        sql2="select user_id, bike_id, take_time, return_time from order_info where id='%d'"%(order_id)
        try:
            cursor.execute(sql2)
            result=cursor.fetchone()
            user_id, bike_id, take_time, return_time=result
        except Exception as e:
            #数据库连接发生错误
            print ("Error: unable to fetch data")
            print (e)

        #bike_info 修改
        sql3="update bike_info set state=1 where id='%d'"%(bike_id)
        try:
            cursor.execute(sql3)
            db.commit()
        except Exception as e:
            db.rollback()
            #数据库连接发生错误
            print ("Error: unable to fetch data")
            print (e)
        


        #user_info 扣费
        #a=datetime.datetime.strptime(take_time, "%Y-%m-%d %H:%M:%S")
        #b=datetime.datetime.strptime(return_time, "%Y-%m-%d %H:%M:%S")
        a=take_time
        b=return_time
        minutes=(b-a).seconds//60
        hour=minutes//60
        minutes=minutes%60

        if minutes==0:
            minutes=1
            
        if 0<minutes<10:
            money=hour+minutes*0.1
        else:
            money=hour+1
        print(money)
        
        sql4="update user_info set money=money-'%f', minutes=minutes+'%d' where id='%d'"%(money, minutes, user_id)
        try:
            cursor.execute(sql4)
            db.commit()
        except Exception as e:
            db.rollback()
            #数据库连接发生错误
            print ("Error: unable to fetch data")
            print (e)


    all_finished()
    tcpCliSock.sendall(message.encode('utf-8'))
    tcpCliSock.close()

def user_enroll(tcpCliSock, recv_message_list):
    #recv_message_list  "enroll_validate"+" "+enroll_name_string+" "+
    #enroll_phone_string+" "+enroll_email_string+" "+enroll_password_string
    message="0"+'\r\n'

    sql="insert into user_info(username, phonenum, email, password) values ('%s', '%s', '%s', '%s')"%(recv_message_list[1], recv_message_list[2], recv_message_list[3], recv_message_list[4])

    try:
        cursor.execute(sql)
        db.commit()
        message="1"+'\r\n'
    except Exception as e:
        db.rollback()
        #数据库连接发生错误
        print ("Error: unable to fetch data")
        print (e)

    tcpCliSock.sendall(message.encode('utf-8'))
    tcpCliSock.close()

if __name__=="__main__":
    newtwork_activate()
    

    
