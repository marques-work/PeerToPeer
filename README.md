# Release_PPCS_3.1.0 20180524:

> (translated using Google Translate)

1. P2P library updated to the latest version 3.1.0.
2. ListenTester_PPCS: test program for simulated device side, including simulated standard P2P platform device side and RTOS P2P platform device side.
    * Simulate standard P2P device-side test: only P2P test, do not support sleep wake test.
    * Simulate RTOS P2P device side test: Wakeup parameters (WakeupKey, WakeupServerIP) need to be brought in. If not, only the RTOS P2P part can be tested.
    PS:
    * Standard P2P platform does not support device wake-up. RTOS P2P platform supports device wake-up.
    * Standard P2P platform supports device forwarding function, RTOS P2P platform does not support device forwarding function.
    * Standard P2P device parameters and RTOS device parameters are two sets of different platform parameters. If you have two sets of platforms at the same time, the parameters cannot be mixed.
3. ConnectionTester: connection test tool. Used to test and analyze the connection effect with the device.
4. ReadWriteTester: read and write transmission test tool, need to cooperate with ListenTester_PPCS to test.
5. For more detailed instructions, please refer to the SDK Release_PPCS_3.1.0 / Doc / directory P2P SDK documentation and P2P technical white paper.

---

### Original text below:

Release_PPCS_3.1.0 20180524：
1.P2P 库更新为最新版本 3.1.0 版。
2.ListenTester_PPCS：模拟设备端的测试程序，包括模拟标准P2P平台设备端与RTOS P2P平台设备端。
  *模拟标准P2P设备端测试：只能测试P2P，不支持休眠唤醒的测试。
  *模拟RTOS P2P设备端测试：需要带入唤醒参数（WakeupKey,WakeupServerIP），若不带，只能测试RTOS P2P部分。
PS:
  *标准P2P平台不支持设备休眠唤醒，RTOS P2P平台支持设备休眠唤醒。
  *标准P2P平台支持设备转发功能，RTOS P2P平台不支持设备转发功能。
  *标准P2P设备端参数与RTOS设备端参数为两套不同平台的参数，若同时拥有这两套平台，则参数不能混用。

3.ConnectionTester：连接测试工具。用来测试分析与设备端的连接效果。
4.ReadWriteTester：读写传输测试工具，需配合 ListenTester_PPCS 来测试。
5.更多详细说明请参考SDK Release_PPCS_3.1.0/Doc/目录下 P2P SDK 说明文档与P2P 技术白皮书。
