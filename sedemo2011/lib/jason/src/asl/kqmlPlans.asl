// Default plans to handle KQML performatives
// Users can override them in their own AS code
// 
// Variables:
//   Sender:  the sender (an atom)
//   Content: content (typically a literal)
//   MsgId:   message id (an atom)
//


/* ---- tell performatives ---- */ 

@kqmlReceivedTellStructure
+!kqml_received(Sender, tell, Content, _) 
   :  .literal(Content) & 
      .ground(Content) &
      not .list(Content)
   <- .add_nested_source(Content, Sender, CA); 
      +CA.
@kqmlReceivedTellList
+!kqml_received(Sender, tell, Content, _) 
   :  .list(Content)
   <- !add_all_kqml_received(Sender,Content).

@kqmlReceivedTellList1
+!add_all_kqml_received(_,[]).   

@kqmlReceivedTellList2
+!add_all_kqml_received(Sender,[H|T])
   :  .literal(H) & 
      .ground(H)
   <- .add_nested_source(H, Sender, CA); 
      +CA;
      !add_all_kqml_received(Sender,T).

@kqmlReceivedTellList3
+!add_all_kqml_received(Sender,[_|T])
   <- !add_all_kqml_received(Sender,T).
      
@kqmlReceivedUnTell
+!kqml_received(Sender, untell, Content, _)
   <- .add_nested_source(Content, Sender, CA); 
      -CA.


/* ---- achieve performatives ---- */ 

@kqmlReceivedAchieve
+!kqml_received(Sender, achieve, Content, _)
    : not .list(Content)
   <- .add_nested_source(Content, Sender, CA); 
      !!CA.
@kqmlReceivedAchieveList
+!kqml_received(Sender, achieve, Content, _)
    : .list(Content)
   <- !add_all_kqml_achieve(Sender,Content).

      
@kqmlReceivedAchieveList1
+!add_all_kqml_achieve(_,[]).   

@kqmlReceivedAchieveList2
+!add_all_kqml_achieve(Sender,[H|T])
   <- .add_nested_source(H, Sender, CA); 
      !!CA;
      !add_all_kqml_achieve(Sender,T).

      
@kqmlReceivedUnAchieve[atomic]
+!kqml_received(_, unachieve, Content, _)
   <- .drop_desire(Content).


/* ---- ask performatives ---- */ 

@kqmlReceivedAskOne1
+!kqml_received(Sender, askOne, Content, MsgId) 
   <- ?Content;
      .send(Sender, tell, Content, MsgId).

@kqmlReceivedAskOne2 // error in askOne, send untell
-!kqml_received(Sender, askOne, Content, MsgId)
   <- .send(Sender, untell, Content, MsgId).      

@kqmlReceivedAskAll2
+!kqml_received(Sender, askAll, Content, MsgId)
   <- .findall(Content, Content, List); 
      .send(Sender, tell, List, MsgId).


/* ---- know-how performatives ---- */ 

// In tellHow, content must be a string representation
// of the plan (or a list of such strings)

@kqmlReceivedTellHow
+!kqml_received(Sender, tellHow, Content, _)
   <- .add_plan(Content, Sender).

// In untellHow, content must be a plan's
// label (or a list of labels)
@kqmlReceivedUnTellHow
+!kqml_received(Sender, untellHow, Content, _)
   <- .remove_plan(Content, Sender).

// In askHow, content must be a string representing
// the triggering event
@kqmlReceivedAskHow
+!kqml_received(Sender, askHow, Content, MsgId)
   <- .relevant_plans(Content, ListOfPlans); 
      .send(Sender, tellHow, ListOfPlans, MsgId).

/* general communication error handler */

@kqmlError 
-!kqml_received(_Sender, _Per, _Content, _MsgId)[error(EID), error_msg(EMsg)] 
   <- .print("Communication error -- ",EID, ": ", EMsg).      
