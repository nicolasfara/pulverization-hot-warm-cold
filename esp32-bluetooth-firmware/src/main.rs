use esp32_nimble::{uuid128, BLEDevice, NimbleProperties};
use esp_idf_sys as _; // If using the `binstart` feature of `esp-idf-sys`, always keep this module imported

fn main() {
    // It is necessary to call this function once. Otherwise some patches to the runtime
    // implemented by esp-idf-sys might not link properly. See https://github.com/esp-rs/esp-idf-template/issues/71
    esp_idf_sys::link_patches();
    esp_idf_svc::log::EspLogger::initialize_default();

    // WDT OFF
    unsafe {
        esp_idf_sys::esp_task_wdt_delete(esp_idf_sys::xTaskGetIdleTaskHandleForCPU(
            esp_idf_hal::cpu::core() as u32,
        ));
    };

    let ble_device = BLEDevice::take();

    let server = ble_device.get_server();
    let service = server.create_service(uuid128!("fafafafa-fafa-fafa-fafa-fafafafafafa"));

    // A static characteristic.
    let static_characteristic = service.lock().create_characteristic(
        uuid128!("d4e0e0d0-1a2b-11e9-ab14-d663bd873d93"),
        NimbleProperties::READ,
    );
    static_characteristic
        .lock()
        .set_value("Hello, world!".as_bytes());

    let ble_advertising = ble_device.get_advertising();
    ble_advertising
        .name("ESP32-GATT-Server")
        .add_service_uuid(uuid128!("fafafafa-fafa-fafa-fafa-fafafafafafa"));

    ble_advertising.start().unwrap();
}
