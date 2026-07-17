document.addEventListener(
    "DOMContentLoaded",
    () => {

        document.body.classList.add("loaded");


        document
            .querySelectorAll(
                "button,.btn,input[type=submit]"
            )
            .forEach(button=>{


                button.addEventListener(
                    "click",
                    e=>{


                        const ripple =
                            document.createElement("span");


                        ripple.className="ripple";


                        button.appendChild(ripple);



                        setTimeout(
                            ()=>ripple.remove(),
                            500
                        );

                    }
                );


            });


        document
            .querySelectorAll(
                ".msg"
            )
            .forEach(msg=>{


                setTimeout(
                    ()=>{

                        msg.style.opacity="0";

                    },
                    3500
                );


            });



    }
);