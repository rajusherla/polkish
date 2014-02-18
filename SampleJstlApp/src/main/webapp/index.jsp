<!doctype html>
<html>
<head>
  <style>
  p {
    margin: 0;
    color: blue;
  }
  div,p {
    margin-left: 10px;
  }
  span {
    color: red;
  }
  </style>
  <script src="http://code.jquery.com/jquery-1.10.2.js"></script>
</head>
<body>

<form action = "/SampleJstlApp/display.do" method= "post">
  <div>
    Enter Text: <input type="text" name="userName" size="25" value="">
    <input type="submit">
  </div>
  <span></span>
  <br>
  <div>
   Submited Text: <input type="text" name="userName2" size="25" value="${textArea.userName}">
  </div>
</form>

 
<script>
$( "form" ).submit(function( event ) {
  if ( $( "input:first" ).val().length > 0 ) {
    
    return;
  }
 
  $( "span" ).text( "please enter the textbox value" ).show();
  event.preventDefault();
});
</script>
 
</body>
</html>