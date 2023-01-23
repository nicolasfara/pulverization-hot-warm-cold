use std::env;
use bluer::adv::Advertisement;
use rppal::pwm::{Channel, Polarity, Pwm};
use std::{net::SocketAddr, time::Duration};
use tokio::{
    io::{AsyncBufReadExt, BufReader},
    net::TcpStream,
    task,
    time::sleep,
};

#[tokio::main(flavor = "current_thread")]
async fn main() -> bluer::Result<()> {
    env_logger::init();

    let args: Vec<String> = env::args().collect();

    let session = bluer::Session::new().await?;
    let adapter = session.default_adapter().await?;
    adapter.set_powered(true).await?;

    println!(
        "Advertising on Bluetooth adapter {} with address {}",
        adapter.name(),
        adapter.address().await?
    );
    let le_advertisement = Advertisement {
        advertisement_type: bluer::adv::Type::Peripheral,
        service_uuids: vec!["123e4567-e89b-12d3-a456-426614174000".parse().unwrap()]
            .into_iter()
            .collect(),
        discoverable: Some(true),
        local_name: Some("le_advertise".to_string()),
        ..Default::default()
    };
    println!("{:?}", &le_advertisement);
    let handle = adapter.advertise(le_advertisement).await?;

    let pwm = Pwm::with_frequency(Channel::Pwm0, 75.0, 0.0, Polarity::Normal, true).unwrap();

    let job = task::spawn(async move {
        let ip = &args[1];
        let addr = format!("{}:8080", ip).parse::<SocketAddr>().unwrap();
        let stream = TcpStream::connect(&addr).await.unwrap();
        let mut stream = BufReader::new(stream);
        loop {
            let mut payload_line = String::new();
            stream.read_line(&mut payload_line).await.unwrap();
            println!("New value: {}", payload_line);
            let payload_line = payload_line.trim();
            match payload_line.parse::<f64>() {
                Ok(value) => {
                    println!("Parsed value: {}", value);
                    pwm.set_frequency(75.0, value).unwrap();
                }
                Err(_) => {
                    println!("Failed to parse")
                }
            }
        }
    });

    let _ = job.await;
    println!("Removing advertisement");
    drop(handle);
    sleep(Duration::from_secs(1)).await;

    Ok(())
}
