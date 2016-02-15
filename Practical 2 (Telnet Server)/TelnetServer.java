/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author Emilio Singh     u14006512
 * @author Renton McIntyre  u14312710
 *
 */
public class TelnetServer {
    private static ArrayList<Socket> users = new ArrayList<Socket>();
    /**
     * @param args the command line arguments
     */
    private static ArrayList<testNode> questions=new ArrayList<testNode>();
    private static ArrayList<currentQuestion> currentQs = new ArrayList<currentQuestion>();

    public static class testNode
    {
        private String question;
        private ArrayList<String> answers=new ArrayList<>();
    };
    
    public static class currentQuestion
    {
        private Socket user;
        private int currQ;
        
        public currentQuestion(Socket u, int i)
        {
            user = u;
            currQ = i;
        }
    }

    public static void main(String[] args) throws IOException
    {

        int port=55555;
        try

        {
            ServerSocket myServer=new ServerSocket(port);
            constructTest();

          while (true)
            {
                Socket client=myServer.accept();
                new TelThread(client).start();
                announce("Welcome User to the Telnet Tester", client);
                //list.add(0, client);
                users.add(client);
                currentQs.add(new currentQuestion(client, -1));
                System.out.println("A new person has joined.");
                ///id++;
             //   ins();

            }
        }
         catch(IOException e)
              {
          System.out.println(e.getMessage());
           }

    }


   synchronized static void announce(String msg, Socket client) throws IOException
    {
        PrintWriter p;
        
            p=new PrintWriter(client.getOutputStream(),true);
            p.println(msg);
    }

    synchronized static void prompt(Socket client) throws IOException
    {
        PrintWriter p;
             p = new PrintWriter(client.getOutputStream(), true);
             p.println("Would you like another question, or would you like to stop here? (1 - Continue, 2 - Stop)");
    }

   synchronized static int message(Socket client) throws IOException
   {
        PrintWriter p;

        Random number=new Random();
        int n=number.nextInt(questions.size());


            p=new PrintWriter(client.getOutputStream(),true);
            p.write(27);
            p.print("[2J");
            p.println(questions.get(n).question);
            char a='A';
            int color = 31;
            for (int i=0;i<questions.get(n).answers.size();i++)
            {
                p.write(27);
                p.print("[1;"+color+"m");
                p.print(a);
                p.write(27);
                p.print("[0m");

                p.println(":"+questions.get(n).answers.get(i).substring(1));
                a++;
                color+=1;
            }
            p.println(" ");

            p.println("Select an option");

            return n;
   }
   synchronized static void leave(Socket client) throws IOException
   {
        PrintWriter p;
            p=new PrintWriter(client.getOutputStream(),true);
            p.println("Tester  has left");
        users.remove(client);
   }
      synchronized static void giveScore(TelThread user, Socket client) throws IOException
   {
        PrintWriter p;
            p=new PrintWriter(client.getOutputStream(),true);
            p.println("Your score is: "+user.score);


   }
  /*    synchronized static void ins(Socket client) throws IOException
      {
        PrintWriter p;
            p=new PrintWriter(tester.getOutputStream(),true);
            p.println(" ");
            p.println("Commands (To type in)");
            p.println(" ");
            p.println("Score- See your score");
            p.println(" ");
            p.println("Next Question - go to the next question of the test");
            p.println(" ");
            p.println("bye- leave the test");
            p.println(" ");

      }*/
      
   synchronized static boolean checkAnswer(String ans, Socket client)
   {

       
       testNode tmp=new testNode();
       tmp.question=questions.get( getQ(client) ).question;
       tmp.answers=questions.get( getQ(client) ).answers;
       int actualAns=-1,answer=-1;
       switch (ans)
       {
           case "A":answer=0;break;
           case "B":answer=1;break;
           case "C":answer=2;break;
           case "D":answer=3;break;
           case "E":answer=4;
       };
       boolean answered=false;

       for(int i=0;i<tmp.answers.size()&& answered==false;i++)
       {
           if(tmp.answers.get(i).startsWith("+") && answered==false)
           {
                              actualAns=i;
           }
           else if(tmp.answers.get(i).startsWith("+More")&& answered==false)
               {
                   actualAns=i;
                   answered=true;
               }
           else if(tmp.answers.get(i).startsWith("+None")&& answered==false)
               {
                   actualAns=i;
                   answered=true;
               }


       }


       return (actualAns==answer);
   }

   synchronized static void correctAns(TelThread user, Socket client) throws IOException
   {
        PrintWriter p;

            p=new PrintWriter(client.getOutputStream(),true);
            p.write(27);
            p.print("[1;36m");
            p.println("Congratulations. You got it right!");
            p.write(27);
            p.print("[0m");
            user.score++;
            p.println(" ");
   }

   synchronized static void wrongAns(int a, Socket client) throws IOException
   {
        PrintWriter p;
        String cAns="";
        for (int i=0;i<questions.get(a).answers.size();i++)
        {
            if (questions.get(a).answers.get(i).startsWith("+"))
                {

                  for (int k=0;k<questions.get(a).answers.size();k++)
                  {
                      if (questions.get(a).answers.get(k).equals("+More than one of the above"))
                      {
                          cAns=questions.get(a).answers.get(k);
                      }
                      if (questions.get(a).answers.get(k).equals("+None of the above"))
                      {
                          cAns=questions.get(a).answers.get(k);
                      }
                  }

               cAns=questions.get(a).answers.get(i);
                }
        }

            p=new PrintWriter(client.getOutputStream(),true);
            p.write(27);
            p.print("[1;36m");
            p.println("The correct answer is:"+ cAns.substring(1));
            p.write(27);
            p.print("[0m");
            p.println(" ");
   }

    private static void constructTest() throws IOException
   {



       FileReader fr=new FileReader("quiz.txt");
       BufferedReader textReader=new BufferedReader(fr);

       String sline;
       int q=0;
       while ((sline=textReader.readLine())!=null)
        {
            textReader.mark(1000);
            if (sline.charAt(0)=='?')
            {
                //textReader.mark(100);
                testNode tmp=new testNode();
                tmp.question=sline;
                //textReader.skip(0);
                while ((sline=textReader.readLine())!=null&&(sline.charAt(0)!='?'))
                {

                    tmp.answers.add(0,sline);
                }
                textReader.reset();
                int onlyRight=0,onlyWrong=0;
                for(int i=0;i<tmp.answers.size();i++)
                {
                   if (tmp.answers.get(i).startsWith("+")==true)
                   {
                       onlyRight++;
                   } else if (tmp.answers.get(i).startsWith("-")==true)
                    {
                        onlyWrong++;
                    }
                }
                if ((onlyRight>1) && (onlyWrong==0))
                {
                    tmp.answers.add("+More than one of the above");
                }

                if ((onlyRight==0) && (onlyWrong>1))
                {
                    tmp.answers.add("+None of the above");
                }
                questions.add(0,tmp);
            }

        }

       }
   synchronized static public void setQ(int a, Socket client)
    {
        for(int i = 0; i < currentQs.size(); ++i)
        {
            if(currentQs.get(i).user == client)
            {
                currentQs.get(i).currQ = a;
            }
        }
    }
   synchronized static public int getQ(Socket client)
    {
        for(int i = 0; i < currentQs.size(); ++i)
        {
           if(currentQs.get(i).user == client)
            {
                return currentQs.get(i).currQ;
            }
        }
        return -1;
    }
}
