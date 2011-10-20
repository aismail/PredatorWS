// this agent starts the auction and identify the winner

/* beliefs and rules */ 

all_bids_received(N) :- .count(place_bid(N,_),3). 

/* plans */

+!start_auction(N) : true      // this goal is created by the GUI of the agent 
    <- -+auction(N);
       -+winner(N, noone, 0);
       .broadcast(tell, auction(N)).

// receive bid and check for new winner
@pb1[atomic]
+place_bid(N,V)[source(S)] 
   :  auction(N) & winner(N,CurWin,CurVl) & V > CurVl
   <- -winner(N,CurWin,CurVl); 
      +winner(N,S,V); .print("New winner is ",S, " with value ",V);
      !check_end(N).

@pb2[atomic]
+place_bid(N,_) : true
   <- !check_end(N).

+!check_end(N) 
   :  all_bids_received(N) & 
      winner(N,W,Vl)
   <- .print("Winner is ",W," with ", Vl);
      show_winner(N,W); // show it in the GUI
      .broadcast(tell, winner(W));
      .abolish(place_bid(N,_)).
+!check_end(_).

