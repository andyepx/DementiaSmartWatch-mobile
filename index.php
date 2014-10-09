<?php
	
	$ch = curl_init();

	curl_setopt($ch, CURLOPT_URL, "https://android.googleapis.com/gcm/send");
	curl_setopt($ch, CURLOPT_HTTPHEADER, array(
		'Authorization:	key=AIzaSyD0i-DTLhc0I-LSh4koKWQGaEUzlVbruY4',
		'Content-Type: application/json'
		));

	$d = array( 'message' => 'Hello World!' );

	$data = array(
		"data" => $d,
		"registration_ids" => array("APA91bHs3WCxwkkClD5FkFcdFEUmL82qFldXQUkNG7sZCjeG_SibL0uXWjqGRQBZLRL9-4qoUEMhd0kBGdzQgUUNTjF1K7SxllJHrOte_KwgWAfFlMWPhzRFC5HZ7qWB30tAiOw4a9BsH6xtWQ_kRJ889vqQKGSALLArXPFsFAONQ791CKUpwSI"));

	curl_setopt($ch, CURLOPT_POST, true );
	curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

	//execute post
	$result = curl_exec($ch);

	if ( curl_errno( $ch ) )
    {
        echo 'GCM error: ' . curl_error( $ch );
    }

	print_r($result);

	//close connection
	curl_close($ch);

?>