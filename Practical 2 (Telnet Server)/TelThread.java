/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author Emilio Singh     u14006512
 * @author Renton McIntyre  u14312710
 *
 */
public class TelThread extends Thread {
    private Socket mySocket;
    public int score;
    TelThread(Socket mySocket) {
        this.mySocket=mySocket;
        this.score=0;
    }


    public void run()
    {
       try{
            PrintWriter out = new PrintWriter(mySocket.getOutputStream(),true);
            BufferedReader in = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
            TelnetServer.setQ(TelnetServer.message(mySocket), mySocket);


                String line;
            while(in!= null && ((line = in.readLine().trim()) != null))
            {

              /*  if (line.equals("bye")) {
                    TelnetServer.leave(mySocket);
                    break;
                  }else if(line.equals("Next Question"))
                {

                       TelnetServer.setQ(TelnetServer.message());
                        //if (TelnetServer.checkAnswer(line,a)==true){TelnetServer.correctAns();} else {TelnetServer.wrongAns(a);}

                }else if (line.equals("Score"))
                {
                    TelnetServer.giveScore(this);
                }
                else
                {*/
                    if (TelnetServer.checkAnswer(line, mySocket)==true)   TelnetServer.correctAns(this, mySocket); 
                    else TelnetServer.wrongAns(TelnetServer.getQ(mySocket), mySocket);

                    TelnetServer.prompt(mySocket);
                    int reply;
                    line = in.readLine().trim();
                    if(line.equals("1"))
                    {
                      TelnetServer.setQ(TelnetServer.message(mySocket), mySocket);
                    }
                    else
                    {
                      TelnetServer.giveScore(this, mySocket);
                      TelnetServer.leave(mySocket);
                      break;
                    }
               //}
            }

            try {
                out.close();
                in.close();
            }catch(IOException e)
            {
                System.out.println(e.getMessage());
            }
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }

    }
}
