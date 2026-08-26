import subprocess
from concurrent.futures import ThreadPoolExecutor, as_completed

def run_command(command):
    """Executes a single system command and captures its output."""
    try:
        # runs the command, captures output, blocks until this specific process ends
        result = subprocess.run(
            command,
            shell=True,
            text=True,
            capture_output=True,
            check=True
        )
        return {"command": command, "status": "Success", "stdout": result.stdout.strip()}
    except subprocess.CalledProcessError as e:
        return {"command": command, "status": "Failed", "stderr": e.stderr.strip()}

# Define the list of system commands you want to run in parallel
command = "/usr/bin/java -jar LifeSim.jar"

# Set the maximum number of worker threads to run simultaneously
MAX_WORKERS = 20

print("Launching subprocesses in parallel...")

# Initialize the thread pool
with ThreadPoolExecutor(max_workers=MAX_WORKERS) as executor:
    # Submit all commands to the executor pool
    future_to_command = {executor.submit(run_command, command): i for i in range(10)}

    # Process results as soon as each individual thread finishes
    for future in as_completed(future_to_command):
        data = future.result()
        print(f"\n[Command]: {data['command']}")
        print(f"[Status]:  {data['status']}")
        if data['status'] == "Success":
            print(f"[Output]:  {data['stdout']}")
        else:
            print(f"[Error]:   {data['stderr']}")
